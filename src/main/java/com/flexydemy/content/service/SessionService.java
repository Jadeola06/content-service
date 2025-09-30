package com.flexydemy.content.service;

import com.flexydemy.content.dto.*;
import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.enums.SessionStatus;
import com.flexydemy.content.enums.SessionType;
import com.flexydemy.content.exceptions.BadRequestException;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.*;
import com.flexydemy.content.repository.*;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final TutorRepository tutorRepository;
    private final CourseRepository courseRepository;
    private final SessionRepository sessionRepository;
    private final EventRepository eventRepository;
    private final UserClient userRepository;
    private final YouTubeService youTubeService;
    private final LiveStreamRepository liveStreamRepository;
    private final TutorDisplaySessionRepository tutorDisplaySessionRepository;
    private final DailyService dailyService;


    @Transactional
    public SessionResponseDTO createSessionByTutor(TutorSessionCreateRequestDTO dto, HttpServletRequest request) {
        String token = Utils.getToken(request);

        if (dto == null) {
            throw new BadRequestException("Request is Null.");
        }

        if (dto.getTutorId().isBlank()) {
            throw new BadRequestException("Request is missing tutor id.");
        }

        Tutor tutor = tutorRepository.findById(dto.getTutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        if (dto.getSessionType().equalsIgnoreCase("ONE_ON_ONE") && dto.getStudentIds().size() != 1) {
            throw new BadRequestException("1-on-1 session must have exactly one student.");
        }

        for (String studentId : dto.getStudentIds()) {
            userRepository.findById(studentId, token)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", studentId)))
                    .block();
        }

        LocalDateTime startTime = parseAndValidateFutureDateTime(dto.getStartDateTime());
        Duration duration = Duration.ofMinutes(dto.getDurationMinutes());

        Session session = new Session();
        session.setTutor(tutor);
        session.setName(dto.getName());
        session.setStudentIds(dto.getStudentIds());
        session.setSessionStatus(SessionStatus.PENDING);
        session.setSessionType(SessionType.valueOf(dto.getSessionType()));
        session.setStartDateTime(startTime);
        session.setEndDateTime(startTime.plus(duration));
        session.setScheduledTime(startTime);
        session.setDuration(duration);
        session.setActive(true);
        session.setLive(false);

        sessionRepository.save(session);

        return new SessionResponseDTO(session, false, null);
    }


    @Transactional
    public SessionResponseDTO requestCustomSession(CustomSessionRequestDTO requestDto, HttpServletRequest request) {
        String token = Utils.getToken(request);
        if (requestDto == null) {
            throw new BadRequestException("Request is Null.");
        }

        if (requestDto.getStudentId().isBlank() || requestDto.getTutorId().isBlank()) {
            throw new BadRequestException("Request is missing tutor id and/or student id.");
        }

        //Confirm student exists
        UserDto student = userRepository.findById(requestDto.getStudentId(), token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", requestDto.getStudentId())))
                .block();

        Tutor tutor = tutorRepository.findById(requestDto.getTutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        // Create a pending 1-on-1 session
        Session session = new Session();
        session.setStudentIds(Collections.singletonList(requestDto.getStudentId()));
        session.setSessionType(SessionType.ONE_ON_ONE);
        assert student != null;
        session.setName("Custom Session with " + student.getFirstName() + " " + student.getLastName());
        Duration duration = parseDuration(requestDto);
        LocalDateTime startTime = parseAndValidateFutureDateTime(requestDto.getStartDateTime());
        session.setTutor(tutor);

        session.setStartDateTime(startTime);
        session.setEndDateTime(startTime.plus(duration));
        session.setScheduledTime(startTime);
        session.setDuration(duration);
        session.setActive(false);
        session.setLive(false);
        session.setSessionStatus(SessionStatus.PENDING);

        session = sessionRepository.save(session);
        return new SessionResponseDTO(session, false, null);
    }

    @Transactional
    public SessionResponseDTO bookMadeSession(BookSessionRequestDTO requestDto, HttpServletRequest request) {
        if (requestDto == null) {
            throw new BadRequestException("Request is Null.");
        }

        if (requestDto.getStudentId().isBlank() || requestDto.getTutorId().isBlank()) {
            throw new BadRequestException("Request is missing tutor id and/or student id.");
        }
        String token = Utils.getToken(request);

        // 1. Confirm student exists
        userRepository.findById(requestDto.getStudentId(), token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", requestDto.getStudentId())))
                .block();

        // 2. Get the template session
        TutorDisplaySession displaySession = tutorDisplaySessionRepository.findById(requestDto.getDisplaySessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session template not found"));

        Tutor tutor = displaySession.getTutor();

        // 3. Create session (inactive, waiting for approval)
        Duration duration = displaySession.getDuration();
        LocalDateTime startTime = parseAndValidateFutureDateTime(requestDto.getStartTime());

        Session session = new Session();
        session.setTutor(tutor);
        session.setStudentIds(Collections.singletonList(requestDto.getStudentId()));
        session.setSessionType(SessionType.ONE_ON_ONE);
        session.setName(displaySession.getName());
        session.setDescription(displaySession.getDescription());
        session.setDuration(duration);
        session.setStartDateTime(startTime);
        session.setEndDateTime(startTime.plus(duration));
        session.setScheduledTime(startTime);
        session.setActive(false);  // Waiting for tutor approval
        session.setLive(false);
        session.setSessionStatus(SessionStatus.PENDING);

        session = sessionRepository.save(session);
        return new SessionResponseDTO(session, false, null);
    }


    @Transactional
    public SessionResponseDTO approveCustomSession(SessionApprovalRequest approvalRequest) {
        if (approvalRequest == null || approvalRequest.getSessionId().isBlank()) {
            throw new BadRequestException("Session Approval Request is Null or Incomplete.");
        }
        // Find the pending session
        Session session = sessionRepository.findById(approvalRequest.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        // Validate and set tutor
        Tutor tutor = tutorRepository.findById(approvalRequest.getTutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        session.setTutor(tutor);
        session.setActive(true);
        session.setLive(false);
        session.setDateAdded(LocalDate.now());
        session.setSessionStatus(SessionStatus.APPROVED);

        sessionRepository.save(session);

        // Create calendar events
        Event studentEvent = new Event(
                null,
                session.getId(),
                null,
                session.getStudentIds().get(0),
                tutor,
                session.getDuration(),
                session.getStartDateTime(),
                session.getEndDateTime()
        );

        Event tutorEvent = new Event(
                null,
                session.getId(),
                null,
                session.getStudentIds().get(0),
                tutor,
                session.getDuration(),
                session.getStartDateTime(),
                session.getEndDateTime()
        );

        eventRepository.save(studentEvent);
        eventRepository.save(tutorEvent);

        return new SessionResponseDTO(session, false, null);
    }


    @Transactional
    public SessionResponseDTO approveBookedSession(SessionApprovalRequest approvalRequest) {
        if (approvalRequest == null || approvalRequest.getSessionId().isBlank()) {
            throw new BadRequestException("Session Approval Request is Null or Incomplete.");
        }
        Session session = sessionRepository.findById(approvalRequest.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (session.isActive()) {
            throw new BadRequestException("Session is already approved.");
        }

        Tutor tutor = tutorRepository.findById(approvalRequest.getTutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        session.setTutor(tutor);
        session.setActive(true);
        session.setLive(false);
        session.setDateAdded(LocalDate.now());
        session.setSessionStatus(SessionStatus.APPROVED);

        sessionRepository.save(session);

        // Create events
        for (String studentId : session.getStudentIds()) {
            Event studentEvent = new Event(null, session.getId(), null, studentId, tutor,
                    session.getDuration(), session.getStartDateTime(), session.getEndDateTime());
            eventRepository.save(studentEvent);
        }

        Event tutorEvent = new Event(null, session.getId(), null, null, tutor,
                session.getDuration(), session.getStartDateTime(), session.getEndDateTime());
        eventRepository.save(tutorEvent);

        return new SessionResponseDTO(session, false, null);
    }

    @Transactional
    public void rejectSession(String sessionId, String tutorId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BadRequestException("Session ID cannot be null or blank.");
        }

        if (tutorId == null || tutorId.isBlank()) {
            throw new BadRequestException("Tutor ID cannot be null or blank.");
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        if (session.getTutor() == null || !session.getTutor().getTutorId().equals(tutorId)) {
            throw new BadRequestException("Tutor is not authorized to reject this session.");
        }

        if (session.isActive()) {
            throw new BadRequestException("Cannot reject an already approved session.");
        }

        session.setSessionStatus(SessionStatus.REJECTED);
        sessionRepository.save(session);
    }


    @Transactional
    public SessionResponseDTO startSessionMeeting(String sessionId, String tutorId, HttpServletRequest request){
        String token = Utils.getToken(request);
        if (tutorId == null) {
            throw new BadRequestException("Tutor Id is null.");
        }

        if (sessionId.isBlank()) {
            throw new BadRequestException("Request is missing session id.");
        }
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));


        if (!session.isActive()) {
            throw new BadRequestException("Session must be active to start a meeting.");
        }

        if (!session.getTutor().getTutorId().equals(tutorId)) {
            throw new BadRequestException("Unauthorized tutor for this session.");
        }

        if (session.isLive()){
            throw new BadRequestException("Session is already live");
        }

        Tutor tutorUser = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));


        UserDto tutor = userRepository.findById(tutorUser.getUserId(), token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tutor", tutorId)))
                .block();

        String roomName = dailyService.createRoom();
        assert tutor != null;
        String meetingToken = dailyService.createMeetingToken(roomName, tutor.getUsername(), true);


        session.setRoomName(roomName);
        session.setLive(true);
        session.setStartDateTime(LocalDateTime.now());
        sessionRepository.save(session);

        return new SessionResponseDTO(session, true, meetingToken);
    }

    public SessionResponseDTO tutorRejoinMeeting(String sessionId, String tutorId, HttpServletRequest request){
        String token = Utils.getToken(request);
        if (tutorId == null) {
            throw new BadRequestException("Tutor Id is null.");
        }

        if (sessionId.isBlank()) {
            throw new BadRequestException("Request is missing session id.");
        }
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));


        if (!session.isActive()) {
            throw new BadRequestException("Session must be active to rejoin the meeting.");
        }

        if (!session.getTutor().getTutorId().equals(tutorId)) {
            throw new BadRequestException("Unauthorized tutor for this session.");
        }


        Tutor tutorUser = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));


        UserDto tutor = userRepository.findById(tutorUser.getUserId(), token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tutor", tutorId)))
                .block();

        String roomName = session.getRoomName();
        if (roomName == null) throw new BadRequestException("Room not created yet. The tutor must start the session first.");

        assert tutor != null;
        String meetingToken = dailyService.createMeetingToken(roomName, tutor.getUsername(), true);


        session.setLive(true);
        session.setStartDateTime(LocalDateTime.now());
        sessionRepository.save(session);

        return new SessionResponseDTO(session, true, meetingToken);
    }

    public SessionResponseDTO joinMeeting(String sessionId, String studentId, HttpServletRequest request){
        String token = Utils.getToken(request);
        if (studentId == null) throw new BadRequestException("Student Id is null.");
        if (sessionId.isBlank()) throw new BadRequestException("Request is missing session id.");

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.isActive()) throw new BadRequestException("Session must be active to join.");
        if (!session.isLive()) throw new BadRequestException("Session must be live to join.");
        if (!session.getStudentIds().contains(studentId))
            throw new BadRequestException("You are not permitted to attend this session.");

        UserDto student = userRepository.findById(studentId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", studentId)))
                .block();

        // Use the existing room created by the tutor
        String roomName = session.getRoomName();
        if (roomName == null) throw new BadRequestException("Room not created yet. The tutor must start the session first.");

        assert student != null;
        String meetingToken = dailyService.createMeetingToken(roomName, student.getUsername(), false);

        return new SessionResponseDTO(session, false, meetingToken);
    }


    public SessionResponseDTO scheduleCourseSession(LiveStreamDTO liveStreamDTO){
        Tutor tutor = tutorRepository.findById(liveStreamDTO.getTutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        Course course = courseRepository.findById(liveStreamDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        LocalDateTime startTime = parseAndValidateFutureDateTime(liveStreamDTO.getStartDateTime());

        Session session = new Session();
        session.setTutor(tutor);
        session.setCourseId(liveStreamDTO.getCourseId());
        session.setStudentIds(course.getEnrolledStudents());
        session.setSessionType(SessionType.GROUP);
        session.setName(liveStreamDTO.getTitle());
        session.setDescription(liveStreamDTO.getDescription());
        session.setDuration(Duration.ofHours(1));
        session.setStartDateTime(startTime);
        session.setEndDateTime(startTime.plus(Duration.ofHours(1)));
        session.setScheduledTime(startTime);
        session.setActive(true);
        session.setLive(false);
        session.setSessionStatus(SessionStatus.APPROVED);


        return new SessionResponseDTO(sessionRepository.save(session), true, null);
    }


    public List<TutorDisplaySessionDTO> getAvailableSessions(String tutorId) {
        if (tutorId.isBlank()) {
            throw new BadRequestException("Request is missing tutor id.");
        }
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with ID: " + tutorId));

        return tutorDisplaySessionRepository.findByTutor_TutorId(tutorId)
                .stream()
                .map(TutorDisplaySessionDTO::new)
                .collect(Collectors.toList());
    }

    public List<SessionResponseDTO> getAllSessionsForTutor(String tutorId, HttpServletRequest request) {
        String token = Utils.getToken(request);

        if (tutorId.isBlank()) {
            throw new BadRequestException("Request is missing tutor id.");
        }
        return sessionRepository.findByTutor_TutorIdAndActiveTrue(tutorId)
        .stream()
                .map(session -> {
                    SessionResponseDTO dto1 = new SessionResponseDTO(session, false, null); // Assuming constructor exists

                    List<SessionStudent> sessionStudents = session.getStudentIds().stream()
                            .map(id -> {
                                UserDto userDto = userRepository.findById(id, token)
                                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", id)))
                                        .block();

                                SessionStudent student = new SessionStudent();
                                student.setId(userDto.getUserId());
                                student.setName(userDto.getFirstName() + " " + userDto.getLastName());
                                student.setProfileImageUrl(userDto.getProfileImageUrl());

                                return student;
                            })
                            .collect(Collectors.toList());

                    dto1.setStudents(sessionStudents);
                    dto1.setTutor(mapTutor(session, token));

                    return dto1;
                }).toList();

    }

    public List<SessionResponseDTO> getRequestedSessions(String tutorId, HttpServletRequest request) {
        String token = Utils.getToken(request);

        if (tutorId.isBlank()) {
            throw new BadRequestException("Request is missing tutor id.");
        }
        return sessionRepository.findByTutor_TutorIdAndActiveFalse(tutorId)
                .stream()
                .map(session -> {
                    SessionResponseDTO dto1 = new SessionResponseDTO(session, false, null); // Assuming constructor exists

                    List<SessionStudent> sessionStudents = session.getStudentIds().stream()
                            .map(id -> {
                                UserDto userDto = userRepository.findById(id, token)
                                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", id)))
                                        .block();

                                SessionStudent student = new SessionStudent();
                                student.setId(userDto.getUserId());
                                student.setName(userDto.getFirstName() + " " + userDto.getLastName());
                                student.setProfileImageUrl(userDto.getProfileImageUrl());

                                return student;
                            })
                            .collect(Collectors.toList());

                    dto1.setStudents(sessionStudents);


                    dto1.setTutor(mapTutor(session, token));

                    return dto1;
                }).collect(Collectors.toList());
    }




    public SessionDashboard getStudentSessionDashboard(String studentId, HttpServletRequest request) {
        String token = Utils.getToken(request);

        UserDto dto = userRepository.findById(studentId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", studentId)))
                .block();

        SessionDashboard sessionDashboard = new SessionDashboard();
        List<Session> upcomingSessions = sessionRepository.findUpcomingSessionsByUserId(studentId, LocalDateTime.now());
        List<Session> pastSessions = sessionRepository.findPastSessionsForStudent(studentId, LocalDateTime.now());

        sessionDashboard.setUpcomingSessions(
                upcomingSessions.stream()
                        .map(session -> {
                            SessionResponseDTO dto1 = new SessionResponseDTO(session, false, null); // Assuming constructor exists

                            List<SessionStudent> sessionStudents = session.getStudentIds().stream()
                                    .map(id -> {
                                        UserDto userDto = userRepository.findById(id, token)
                                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", id)))
                                                .block();

                                        SessionStudent student = new SessionStudent();
                                        student.setId(userDto.getUserId());
                                        student.setName(userDto.getFirstName() + " " + userDto.getLastName());
                                        student.setProfileImageUrl(userDto.getProfileImageUrl());

                                        return student;
                                    })
                                    .collect(Collectors.toList());

                            dto1.setStudents(sessionStudents);


                            dto1.setTutor(mapTutor(session, token));



                            return dto1;
                        })
                        .collect(Collectors.toList())
        );

        sessionDashboard.setRecentSessions(
                pastSessions.stream()
                        .map(session -> {
                            SessionResponseDTO dto2 = new SessionResponseDTO(session, false, null);

                            List<SessionStudent> sessionStudents = session.getStudentIds().stream()
                                    .map(id -> {
                                        UserDto userDto = userRepository.findById(id, token)
                                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", studentId)))
                                                .block();

                                        SessionStudent student = new SessionStudent();
                                        student.setId(userDto.getUserId());
                                        student.setName(userDto.getFirstName() + " " + userDto.getLastName());
                                        student.setProfileImageUrl(userDto.getProfileImageUrl());

                                        return student;
                                    })
                                    .collect(Collectors.toList());

                            dto2.setStudents(sessionStudents);


                            dto2.setTutor(mapTutor(session, token));
                            return dto2;
                        })
                        .collect(Collectors.toList())
        );

        List<SimpleTutorDTO> tutors = searchTutorsBySubject(dto.getClassCategory().toString(), 0 , 5, request);
        sessionDashboard.setAvailableTutors(tutors);

        return sessionDashboard;
    }

    private List<SimpleTutorDTO> searchTutorsBySubject(String subjectName, int page, int size, HttpServletRequest request) {
        String token = Utils.getToken(request);

        Class_Categories subject;
        try {
            subject = Class_Categories.valueOf(subjectName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return List.of(); // Invalid subject
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Tutor> tutorPage = tutorRepository.findByAreaOfExpertiseContaining(subject, pageable);

        return tutorPage.getContent().stream()
                .map(tutor -> mapToSimpleDTO(tutor, subject, token))
                .collect(Collectors.toList());
    }

    private SessionStudent mapTutor(Session session, String token) {
        String tutorId = Optional.ofNullable(session.getTutor())
                .map(Tutor::getUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", "Unknown"));

        UserDto tutor = userRepository.findById(tutorId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tutor", tutorId)))
                .block();

        SessionStudent tutorDto = new SessionStudent();
        tutorDto.setId(tutor.getUserId());
        tutorDto.setName(tutor.getFirstName() + " " + tutor.getLastName());
        tutorDto.setProfileImageUrl(tutor.getProfileImageUrl());

        return tutorDto;
    }
    private SimpleTutorDTO mapToSimpleDTO(Tutor tutor, Class_Categories subject, String token) {
        UserDto tutorA = userRepository.findById(tutor.getUserId(), token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tutor", tutor.getUserId())))
                .block();
        assert tutorA != null;
        return new SimpleTutorDTO(
                tutor.getTutorId(),
                tutorA.getFirstName() + " " + tutorA.getLastName(),
                tutor.getProfileImageUrl(),
                tutor.getAverageRating(), // assuming getRating() returns int
                LocalDateTime.now(), // assuming this exists
                subject.name(), // or tutor.getSubject() if stored differently
                "2 Years" // assuming this is a string description
        );
    }




    private Duration parseDuration(CustomSessionRequestDTO dto) {
        if (dto.getDurationMinutes() > 0) {
            return Duration.ofMinutes(dto.getDurationMinutes());
        } else {
            throw new BadRequestException("Duration must be provided.");
        }
    }

    private LocalDateTime parseAndValidateFutureDateTime(String dateTimeStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);
            if (dateTime.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("The provided date and time must be in the future.");
            }
            return dateTime;
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date time format. Expected format: yyyy-MM-dd'T'HH:mm:ss");
        }
    }
}
