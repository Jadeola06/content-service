package com.flexydemy.content.service;

import com.cloudinary.Cloudinary;
import com.flexydemy.content.dto.*;
import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.enums.SessionStatus;
import com.flexydemy.content.enums.TutorStatus;
import com.flexydemy.content.exceptions.BadRequestException;
import com.flexydemy.content.exceptions.RatingException;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.*;
import com.flexydemy.content.repository.*;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.ResumeParser;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.utils.ObjectUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TutorService {

    private final TutorRepository tutorRepository;
    private final UserClient userRepository;
    private final TutorRatingRepository tutorRatingRepository;
    private final StudentCourseProgressRepository studentCourseProgressRepository;

    private final SessionRepository sessionRepository;

    private final CourseRepository courseRepository;
    private final MessageRepository messageRepository;

    private final ResumeParser resumeParser;

    private final EarningsService earningsService;
    private final SessionService sessionService;


    @Autowired
    private Cloudinary cloudinary;


    @Autowired
    public TutorService(TutorRepository tutorRepository, UserClient userRepository,
                        TutorRatingRepository tutorRatingRepository, StudentCourseProgressRepository studentCourseProgressRepository, SessionRepository sessionRepository, CourseRepository courseRepository, MessageRepository messageRepository, ResumeParser resumeParser, EarningsService earningsService, SessionService sessionService) {
        this.tutorRepository = tutorRepository;
        this.userRepository = userRepository;
        this.tutorRatingRepository = tutorRatingRepository;
        this.studentCourseProgressRepository = studentCourseProgressRepository;
        this.sessionRepository = sessionRepository;
        this.courseRepository = courseRepository;
        this.messageRepository = messageRepository;
        this.resumeParser = resumeParser;
        this.earningsService = earningsService;
        this.sessionService = sessionService;
    }

    public TutorDTO registerTutor(RegisterTutorDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Tutor registration data is required.");
        }
        System.out.println("hello");


        String userId = dto.getUserId();

        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("User ID is required for tutor registration.");
        }


        Tutor tutor = new Tutor();
        tutor.setUserId(dto.getUserId());

        tutor.setStatus(TutorStatus.PENDING);
        tutor.setVerified(false);
        tutor.setRatingsCount(0);
        tutor.setAverageRating(0.0);
        tutor.setTotalSubscribers(0L);

        Tutor savedTutor = tutorRepository.save(tutor);

        TutorDTO newDto = new TutorDTO();
        newDto.setTutorId(savedTutor.getTutorId());

        return newDto;
    }

    public TutorDTO updateTutorResume(String tutorId, MultipartFile file,HttpServletRequest request){
        String token = Utils.getToken(request);

        if (tutorId.isEmpty()) {
            throw new BadRequestException("Tutor Id is necessary");
        }

        Tutor tutor = validateTutorId(tutorId);

        if (file.isEmpty() && (tutor.isResumeCollected())){
            throw new BadRequestException("Resume is not included");
        }

        UpdateTutorDTO extractedInfo;
        try {
            String content = resumeParser.parseResume(file);
            extractedInfo = resumeParser.extractTutorInfo(content);

            tutor.setResumeCollected(true);
        } catch (Exception e) {
            throw new RuntimeException("Error Reading Resume", e);
        }

        tutor.setQualifications(extractedInfo.getQualifications());
        tutor.setAreaOfExpertise(extractedInfo.getAreaOfExpertise());
        tutor.setSchools(extractedInfo.getSchools());

        return mapToDTO(tutorRepository.save(tutor), token);

    }


    public TutorDTO updateTutor(UpdateTutorDTO dto, HttpServletRequest request) {
        String token = Utils.getToken(request);
        if (dto == null ) {
            throw new BadRequestException("Tutor Properties are required for rating.");
        }
        if (dto.getTutorId().isEmpty()) {
            throw new BadRequestException("Tutor Id is necessary");
        }


        Tutor tutor = validateTutorId(dto.getTutorId());


        tutor.setBio(dto.getBio());
        tutor.setQualifications(dto.getQualifications());
        tutor.setAreaOfExpertise(dto.getAreaOfExpertise());
        tutor.setSchools(dto.getSchools());
        tutor.setWorkingDays(dto.getWorkingDays());
        tutor.setWorkExperiences(dto.getWorkExperiences());

        tutor.setXUrl(dto.getXUrl());
        tutor.setLinkedinUrl(dto.getLinkedinUrl());
        tutor.setFacebookUrl(dto.getFacebookUrl());

        return mapToDTO(tutorRepository.save(tutor), token);
    }


    public void deleteTutor(String tutorId) {
        validateTutorId(tutorId);

        if (!tutorRepository.existsById(tutorId)) {
            throw new ResourceNotFoundException("Tutor not found");
        }
        tutorRepository.deleteById(tutorId);
    }

    public TutorDTO getTutorById(String tutorId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        validateTutorId(tutorId);
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));
        return mapToDTO(tutor, token);
    }


    public List<TutorDTO> getAllTutors(HttpServletRequest request) {
        String token = Utils.getToken(request);
        return tutorRepository.findAll()
                .stream()
                .map(tutor -> mapToDTO(tutor, token))
                .toList();
    }


    @Transactional
    public String rateTutor(RatingDTO dto, HttpServletRequest request) {

        String token = Utils.getToken(request);
        if (dto.getTutorId().isEmpty() || dto.getTutorId().isBlank()) {
            throw new BadRequestException("Tutor Id is needed.");
        }
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5.");
        }
        if (dto.getFeedback().isEmpty() || dto.getFeedback().isBlank()) {
            throw new BadRequestException("Feedback is necessary.");
        }
        if (dto.getUserId().isBlank() || dto.getUserId().isEmpty()) {
            throw new BadRequestException("User Id for rater is needed.");
        }
        if (dto.getUsername().isBlank() || dto.getUsername().isEmpty()) {
            throw new BadRequestException("User Name for rater is needed.");
        }

        Tutor tutor = validateTutorId(dto.getTutorId());

        UserDto student = userRepository.findById(dto.getUserId(), token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student - To Rate", dto.getUserId())))
                .block();

        boolean alreadyRated = tutorRatingRepository.existsByStudentIdAndTutor(student.getUserId(), tutor);
        if (alreadyRated) {
            throw new RatingException("You have already rated this tutor.");
        }

        TutorRating tutorRating = new TutorRating();
        tutorRating.setTutor(tutor);
        tutorRating.setStudentId(student.getUserId());
        tutorRating.setStudentUsername(student.getUsername());
        tutorRating.setRating(dto.getRating());
        tutorRating.setFeedback(dto.getFeedback());
        tutorRating.setTime(LocalDateTime.now());

        tutor.setRatingsCount(tutor.getRatingsCount() + 1);
        tutor.setAverageRating(calculateAverageRating(dto.getTutorId()));

        tutorRepository.save(tutor);
        tutorRatingRepository.save(tutorRating);

        return "Tutor rated successfully.";
    }


    public List<RatingDTO> getRatingsForTutor(String tutorId) {
        validateTutorId(tutorId);

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        List<TutorRating> ratings = tutorRatingRepository.findByTutor(tutor);

        return ratings.stream().map(rating -> {
            return new RatingDTO(rating.getStudentId(), rating.getStudentUsername(), rating.getRating(), rating.getFeedback(), rating.getTime());
        }).toList();
    }


    public double calculateAverageRating(String tutorId) {
        validateTutorId(tutorId);
        List<TutorRating> ratings = tutorRatingRepository.findByTutor_TutorId(tutorId);
        return ratings.stream()
                .mapToInt(TutorRating::getRating)
                .average()
                .orElse(0.0);
    }

    public List<TutorDTO> searchTutorsBySubject(String subjectName, int page, int size, HttpServletRequest request) {
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
                .map(tutor -> mapToDTO(tutor, token))
                .collect(Collectors.toList());
    }

    public List<TutorStudentSummaryDTO> getAllTutorStudents(String tutorId, HttpServletRequest request) {
        validateTutorId(tutorId);

        String token = Utils.getToken(request);
        List<StudentCourseProgress> progressList = studentCourseProgressRepository.findByCourse_Tutor_TutorId(tutorId);

        Map<String, TutorStudentSummaryDTO> studentSummaryMap = new HashMap<>();

        for (StudentCourseProgress progress : progressList) {
            String studentId = progress.getUserId();

            UserDto student = userRepository.findById(studentId, token)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", studentId)))
                    .block();

            Course course = progress.getCourse();

            if (student == null || course == null) continue;

            String key = studentId + "-" + course.getCourseId();
            if (studentSummaryMap.containsKey(key)) continue;

            int sessionCount = sessionRepository.countByTutor_TutorIdAndStudentIdsContaining(tutorId, student.getUserId());

            TutorStudentSummaryDTO dto = new TutorStudentSummaryDTO();
            dto.setStudentId(student.getUserId());
            dto.setStudentName(student.getFirstName() + " " + student.getLastName());
            dto.setCourseId(course.getCourseId());
            dto.setCourseTitle(course.getCourseTitle());
            dto.setCourseCategory(course.getSubject());
            dto.setProgressPercentage(progress.getProgressPercentage());
            dto.setSessionCountWithTutor(sessionCount);
            dto.setRating(student.getRating());

            studentSummaryMap.put(key, dto);
        }

        return new ArrayList<>(studentSummaryMap.values());
    }

    public List<TutorStudentSummaryDTO> searchTutorStudents(String tutorId, String keyword, HttpServletRequest request){
        validateTutorId(tutorId);

        String lowerKeyword = keyword.toLowerCase().trim();

        List<TutorStudentSummaryDTO> allStudents = getAllTutorStudents(tutorId, request);

        return allStudents.stream()
                .filter(dto -> {
                    String studentName = dto.getStudentName() != null ? dto.getStudentName().toLowerCase() : "";
                    String courseTitle = dto.getCourseTitle() != null ? dto.getCourseTitle().toLowerCase() : "";
                    return studentName.contains(lowerKeyword) || courseTitle.contains(lowerKeyword);
                })
                .collect(Collectors.toList());
    }

    public TutorDashboardDTO getTutorDashboard(String tutorId, HttpServletRequest request) {
        validateTutorId(tutorId);
        String token = Utils.getToken(request);
        TutorDashboardDTO dashboard = new TutorDashboardDTO();
        dashboard.setTutorId(tutorId);

        // ==== Students & Completed Classes ====
//        List<StudentCourseProgress> progressList = studentCourseProgressRepository.findByCourse_Tutor_TutorId(tutorId);
//        Set<String> uniqueStudentIds = new HashSet<>();
//        int completedClasses = 0;
//
//        for (StudentCourseProgress progress : progressList) {
//            uniqueStudentIds.add(progress.getUserId());
//            if (progress.isCompleted()) completedClasses++;
//        }
//
//        dashboard.setStudents(uniqueStudentIds.size());
        //dashboard.setCompletedClasses(completedClasses);

        // ==== Earnings ====
        dashboard.setEarnings(earningsService.getTotalEarnings(tutorId, token));
        dashboard.setEarningsByMonth(earningsService.getMonthlyBreakdown(tutorId, token));

        // ==== Upcoming Sessions (Cap to 4) ====
        List<Session> upcomingSessions = sessionRepository
                .findTop4ByTutor_TutorIdAndScheduledTimeAfterOrderByScheduledTimeAsc(tutorId, LocalDateTime.now());
        List<TutorUpcomingSessions> upcomingDTOs = upcomingSessions.stream().map(session -> {
            TutorUpcomingSessions dto = new TutorUpcomingSessions();
            dto.setSessionId(session.getId());
            dto.setName(session.getName());
            dto.setTime(session.getScheduledTime());
            return dto;
        }).collect(Collectors.toList());
        dashboard.setUpcomingSessions(upcomingDTOs);

        // ==== Students to Contact (Cap to 3) ====
        List<TutorStudentSummaryDTO> toContact = findStudentsToContact(tutorId, 3, token);
        dashboard.setStudentsToContact(toContact);

        // ==== Messages (Mocked or Retrieved from message system) ====
        List<Message> latestMessages = messageRepository.findTop3ByReceiverIdOrderBySentAtDesc(tutorId);

        List<TutorStudentSummaryDTO> messages = latestMessages.stream().map(msg -> {
            UserDto student = userRepository.findById(msg.getSenderId(), Utils.getToken(request))
                    .blockOptional()
                    .orElse(null);

            TutorStudentSummaryDTO dto = new TutorStudentSummaryDTO();
            dto.setStudentId(msg.getSenderId());
            if (student != null) {
                dto.setStudentName(student.getFirstName() + " " + student.getLastName());
                dto.setProfilePictureUrl(student.getProfileImageUrl());
                dto.setExam(student.getExam());
            }
            dto.setLastMessageTime(msg.getSentAt());
            return dto;
        }).collect(Collectors.toList());

        dashboard.setMessages(messages);

        // ==== Ratings (Cap 3) ====
        List<TutorRating> topRatings = tutorRatingRepository.findTop3ByTutor_TutorIdOrderByTimeDesc(tutorId);

        List<RatingDTO> ratingDTOs = topRatings.stream().map(rating -> {
            RatingDTO dto = new RatingDTO();
            dto.setUserId(rating.getStudentId());
            dto.setUsername(rating.getStudentUsername()); // Assuming you store this
            dto.setCourseId(null); // If you don't track course in TutorRating
            dto.setRating(rating.getRating());
            dto.setFeedback(rating.getFeedback());
            dto.setTime(rating.getTime()); // assuming there's a getTime()
            return dto;
        }).collect(Collectors.toList());

        dashboard.setRatings(ratingDTOs);

        // ==== Sessions This Month & Comparison ====
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusNanos(1);

        int thisMonthSessions = sessionRepository.countByTutor_TutorIdAndStartDateTimeBetween(tutorId, startOfMonth, LocalDateTime.now());
        int lastMonthSessions = sessionRepository.countByTutor_TutorIdAndStartDateTimeBetween(tutorId, startOfLastMonth, endOfLastMonth);

        dashboard.setSessionsThisMonth(thisMonthSessions);

        double changePercent = 0;
        if (lastMonthSessions > 0) {
            changePercent = ((double)(thisMonthSessions - lastMonthSessions) / lastMonthSessions) * 100;
        }
        dashboard.setPercentageComparedToLastMonth(changePercent);

        // ==== Average Rating ====
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", tutorId));
        dashboard.setAverageRating(tutor.getAverageRating());

        // ==== Top Subject (Mock logic, replace with dynamic logic if needed) ====
        dashboard.setTopSubject(tutor.getAreaOfExpertise() != null && !tutor.getAreaOfExpertise().isEmpty()
                ? tutor.getAreaOfExpertise().get(0) : null);

        return dashboard;
    }

    public Page<TutorSessionStudentDTO> getAllTutorSessions(String tutorId, String token, int page, int size) {
        validateTutorId(tutorId);
        Page<Session> sessions = sessionRepository.findByTutor_TutorId(tutorId, PageRequest.of(page, size));
        return mapSessionsToStudentDTOs(sessions, token);
    }

    public Page<TutorSessionStudentDTO> getUpcomingSessions(String tutorId, String token, int page, int size) {
        validateTutorId(tutorId);
        Page<Session> sessions = sessionRepository.findByTutor_TutorIdAndScheduledTimeAfter(
                tutorId, LocalDateTime.now(), PageRequest.of(page, size));
        return mapSessionsToStudentDTOs(sessions, token);
    }

    public Page<TutorSessionStudentDTO> getCompletedSessions(String tutorId, String token, int page, int size) {
        validateTutorId(tutorId);
        Page<Session> sessions = sessionRepository.findByTutor_TutorIdAndSessionStatus(
                tutorId, SessionStatus.COMPLETED, PageRequest.of(page, size));
        return mapSessionsToStudentDTOs(sessions, token);
    }

    public Page<TutorSessionStudentDTO> getMissedSessions(String tutorId, String token, int page, int size) {
        validateTutorId(tutorId);
        Page<Session> sessions = sessionRepository.findByTutor_TutorIdAndSessionStatus(
                tutorId, SessionStatus.MISSED, PageRequest.of(page, size));
        return mapSessionsToStudentDTOs(sessions, token);
    }

    public List<TutorCourseAnalyticsDTO> getTutorCourseAnalytics(String tutorId) {
        validateTutorId(tutorId);
        List<Course> courses = courseRepository.findByTutor_TutorId(tutorId);
        List<TutorCourseAnalyticsDTO> result = new ArrayList<>();

        for (Course course : courses) {
            String courseId = course.getCourseId();

            // Count students enrolled in this course
            int studentCount = studentCourseProgressRepository.countByCourse_CourseId(courseId);

            // Average rating
            double avgRating = tutorRatingRepository.findByTutor_TutorId(tutorId).stream()
                    .filter(r -> courseId.equals(r.getId()))
                    .mapToInt(r -> r.getRating())
                    .average().orElse(0.0);

            // Count sessions associated with this course
            int sessionCount = sessionRepository.countByTutor_TutorIdAndCourseId(tutorId, course.getCourseId());

            // Sessions last month vs this month
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
            LocalDateTime endOfLastMonth = startOfThisMonth.minusSeconds(1);

            int sessionsThisMonth = sessionRepository.countByTutor_TutorIdAndScheduledTimeBetween(
                    tutorId, startOfThisMonth, now);

            int sessionsLastMonth = sessionRepository.countByTutor_TutorIdAndScheduledTimeBetween(
                    tutorId, startOfLastMonth, endOfLastMonth);

            double percentageChange = 0.0;
            if (sessionsLastMonth > 0) {
                percentageChange = ((double) (sessionsThisMonth - sessionsLastMonth) / sessionsLastMonth) * 100;
            }

            // Status logic
            String status;
            if (percentageChange >= 20) {
                status = "High";
            } else if (percentageChange <= -20) {
                status = "Low";
            } else {
                status = "Steady";
            }

            TutorCourseAnalyticsDTO dto = new TutorCourseAnalyticsDTO();
            dto.setCourseId(courseId);
            dto.setCourseTitle(course.getCourseTitle());
            dto.setStudentCount(studentCount);
            dto.setAverageRating(avgRating);
            dto.setSessionCount(sessionCount);
            dto.setPercentageChangeFromLastMonth(Math.round(percentageChange * 100.0) / 100.0);
            dto.setStatus(status);

            result.add(dto);
        }

        return result;
    }




    private List<TutorStudentSummaryDTO> findStudentsToContact(String tutorId, int limit, String token) {
        List<StudentCourseProgress> progresses = studentCourseProgressRepository.findByCourse_Tutor_TutorId(tutorId);

        Map<String, StudentCourseProgress> latestProgressPerStudent = new LinkedHashMap<>();

        for (StudentCourseProgress progress : progresses) {
            String studentId = progress.getUserId();
            if (!latestProgressPerStudent.containsKey(studentId)) {
                latestProgressPerStudent.put(studentId, progress); // take first/latest
            }
        }

        List<TutorStudentSummaryDTO> result = new ArrayList<>();

        for (Map.Entry<String, StudentCourseProgress> entry : latestProgressPerStudent.entrySet()) {
            if (result.size() >= limit) break;

            StudentCourseProgress progress = entry.getValue();
            String studentId = entry.getKey();
            Course course = progress.getCourse();

            // Determine classification
            String classifier;
            if (progress.getProgressPercentage() < 5) {
                classifier = "New Student";
            } else {
                // Check session history or message history
                int sessionCount = sessionRepository.countByTutor_TutorIdAndStudentIdsContaining(tutorId, studentId);
                if (sessionCount >= 1) {
                    classifier = "Returning Student";
                } else {
                    continue; // Not new or returning
                }
            }

            // Fetch student profile
            UserDto student = userRepository.findById(studentId, token)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", studentId)))
                    .block();

            TutorStudentSummaryDTO dto = new TutorStudentSummaryDTO();
            dto.setStudentId(studentId);
            dto.setStudentName(student.getFirstName() + " " + student.getLastName());
            dto.setProfilePictureUrl(student.getProfileImageUrl());
            dto.setExam(student.getExam());
            dto.setClassifier(classifier);
            dto.setCourseId(course.getCourseId());
            dto.setCourseTitle(course.getCourseTitle());
            dto.setCourseCategory(course.getSubject());

            result.add(dto);
        }

        return result;
    }

    public ResponseEntity<?> uploadResume(MultipartFile file, String tutorId) throws IOException {
        validateTutorId(tutorId);

        // Allowed file types (MIME types)
        String[] allowedMimeTypes = {
                "image/jpeg",
                "image/png",
                "image/jpg",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };

        // Maximum file size (5MB)
        long maxSizeInBytes = 5 * 1024 * 1024; // 5MB

        // Check if the file exceeds the maximum allowed size
        if (file.getSize() > maxSizeInBytes) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("File is too large. Maximum file size is 5MB.");
        }

        // Get the type of the uploaded file
        String mimeType = file.getContentType();

        // Check if the file is an allowed type
        if (Arrays.asList(allowedMimeTypes).contains(mimeType)) {

            // Detect resource type based on file type
            String resourceType = mimeType.startsWith("image/") ? "image" : "raw";

            Map<String, Object> uploadResult = cloudinary.uploader()
                    .upload(file.getBytes(), ObjectUtils.asMap("resource_type", resourceType));

            String url = (String) uploadResult.get("secure_url");
            Tutor tutor = tutorRepository.findById(tutorId).get();
            tutor.setResumeCollected(true);
            tutor.setResumeUrl(url);

            tutorRepository.save(tutor);

            return ResponseEntity.ok(url);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file type. Allowed: JPEG, PNG, PDF, DOC, DOCX.");
        }
    }



    private Page<TutorSessionStudentDTO> mapSessionsToStudentDTOs(Page<Session> sessionPage, String token) {
        List<TutorSessionStudentDTO> dtoList = sessionPage.stream().flatMap(session ->
                session.getStudentIds().stream().map(studentId -> {
                    UserDto student = userRepository.findById(studentId, token)
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", studentId)))
                            .block();

                    if (student == null) return null;

                    String courseTitle = studentCourseProgressRepository
                            .findByUserIdAndCourse_Tutor_TutorId(studentId, session.getTutor().getTutorId())
                            .stream()
                            .findFirst()
                            .map(p -> p.getCourse().getCourseTitle())
                            .orElse(null);

                    TutorSessionStudentDTO dto = new TutorSessionStudentDTO();
                    dto.setStudentId(student.getUserId());
                    dto.setStudentName(student.getFirstName() + " " + student.getLastName());
                    dto.setEmail(student.getEmail());
                    dto.setPhoneNumber(student.getPhoneNumber());
                    dto.setExamType(student.getExam());
                    dto.setCourseTitle(courseTitle);
                    dto.setSessionDuration(session.getDuration());
                    dto.setSessionStatus(session.getSessionStatus());
                    dto.setScheduledDate(session.getScheduledTime());

                    return dto;
                })
        ).filter(Objects::nonNull).toList();

        return new PageImpl<>(dtoList, sessionPage.getPageable(), sessionPage.getTotalElements());
    }


    private Tutor validateTutorId(String tutorId) {
        if (tutorId == null || tutorId.trim().isEmpty()) {
            throw new BadRequestException("Tutor ID must not be null or empty.");
        }

        return tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", tutorId));
    }

    public TutorDTO mapToDTO(Tutor tutor, String token) {
        UserDto tutorA = userRepository.findById(tutor.getUserId(), token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tutor", tutor.getUserId())))
                .block();
        assert tutorA != null;


        TutorDTO dto = new TutorDTO();
        dto.setFullName(tutorA.getFirstName() + " " + tutorA.getLastName());
        dto.setTutorId(tutor.getTutorId());
        dto.setUserId(tutor.getUserId());
        dto.setBio(tutor.getBio());
        dto.setProfileVideoUrl(tutor.getProfileVideoUrl());
        dto.setVerified(tutor.isVerified());
        dto.setTotalSubscribers(tutor.getTotalSubscribers());
        dto.setSchools(tutor.getSchools());
        dto.setAreaOfExpertise(tutor.getAreaOfExpertise());
        dto.setQualifications(tutor.getQualifications());
        dto.setWorkingDays(tutor.getWorkingDays());
        dto.setWorkExperiences(tutor.getWorkExperiences());
        dto.setSessionCount(sessionRepository.countByTutor_TutorId(tutor.getTutorId()));
        dto.setProfileImageUrl(tutorA.getProfileImageUrl());
        dto.setAge(Period.between(tutorA.getDob(), LocalDate.now()).getYears());

        dto.setAvailableSessions(sessionService.getAvailableSessions(tutor.getTutorId()));
        dto.setReviews(getRatingsForTutor(tutor.getTutorId()));

        dto.setLinkedinUrl(tutor.getLinkedinUrl());
        dto.setXUrl(tutor.getXUrl());
        dto.setFacebookUrl(tutor.getFacebookUrl());

        return dto;
    }

    public String getTutorResume(String tutorId) {
        Tutor tutor = validateTutorId(tutorId);
        return tutor.getResumeUrl();
    }
}
