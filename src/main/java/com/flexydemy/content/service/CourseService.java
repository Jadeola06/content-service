package com.flexydemy.content.service;

import com.flexydemy.content.dto.*;
import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.exceptions.BadRequestException;
import com.flexydemy.content.exceptions.QuizCreationException;
import com.flexydemy.content.exceptions.RatingException;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.*;
import com.flexydemy.content.dto.UserDto;
import com.flexydemy.content.repository.*;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final WebClient webClient;

    private final CourseRepository courseRepository;
    private final TutorRepository tutorRepository;
    private final UserClient userRepository;
    private final CourseRatingRepository courseRatingRepository;
    private final LessonRepository lessonRepository;
    private final StudentCourseProgressRepository progressRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final MockExamRepository mockExamRepository;
    private final ResourceDocumentRepository resourceDocumentRepository;

    private final MockExamStudentProgressRepository mockExamStudentProgressRepository;
    private final VideoRepository videoRepository;


    private final YouTubeService youTubeService;

    private final TutorService tutorService;
    private final SessionService sessionService;

    private final LiveStreamRepository liveStreamRepository;



    @Autowired
    public CourseService(WebClient.Builder builder, @Value("${question-service.url}") String questionServiceUrl, CourseRepository courseRepository,
                         TutorRepository tutorRepository,
                         UserClient userRepository,
                         CourseRatingRepository courseRatingRepository, LessonRepository lessonRepository, StudentCourseProgressRepository progressRepository, ClassScheduleRepository classScheduleRepository, MockExamRepository mockExamRepository, ResourceDocumentRepository resourceDocumentRepository, MockExamStudentProgressRepository mockExamStudentProgressRepository, VideoRepository videoRepository, YouTubeService youTubeService, TutorService tutorService, SessionService sessionService, LiveStreamRepository liveStreamRepository) {
        this.resourceDocumentRepository = resourceDocumentRepository;
        this.mockExamStudentProgressRepository = mockExamStudentProgressRepository;
        this.videoRepository = videoRepository;
        this.tutorService = tutorService;
        this.sessionService = sessionService;
        this.webClient = builder.baseUrl(questionServiceUrl).build();
        this.courseRepository = courseRepository;
        this.tutorRepository = tutorRepository;
        this.userRepository = userRepository;
        this.courseRatingRepository = courseRatingRepository;
        this.lessonRepository = lessonRepository;
        this.progressRepository = progressRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.mockExamRepository = mockExamRepository;
        this.youTubeService = youTubeService;
        this.liveStreamRepository = liveStreamRepository;
    }
    @Transactional
    public CourseDTO createCourse(CourseDTO dto) {
        if (dto.getCourseTitle() == null || dto.getCourseTitle().isEmpty()) {
            throw new BadRequestException("Course title is required.");
        }
        if (dto.getTutorId() == null || dto.getTutorId().isEmpty()) {
            throw new BadRequestException("Tutor Id is required.");
        }

        Tutor tutor = tutorRepository.findById(dto.getTutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        // Create Course
        Course course = new Course();
        course.setCourseTitle(dto.getCourseTitle());
        course.setDescription(dto.getDescription());
        course.setSubject(dto.getSubjectCategory());
        course.setGradeLevel(dto.getGradeLevel());
        course.setPublished(dto.isPublished());
        course.setDuration(dto.getDuration());
        course.setTutor(tutor);
        course.setRatingsCount(0);
        course.setAverageRating(0.0);

        Course savedCourse = courseRepository.save(course);


        ClassSchedule schedule = new ClassSchedule();
        schedule.setCourse(savedCourse);
        schedule.setTutor(tutor);


        classScheduleRepository.save(schedule);


        // Build and return CourseDTO
        CourseDTO result = new CourseDTO();
        result.setCourseId(savedCourse.getCourseId());
        result.setCourseTitle(savedCourse.getCourseTitle());
        result.setDescription(savedCourse.getDescription());
        result.setSubjectCategory(savedCourse.getSubject());
        result.setGradeLevel(savedCourse.getGradeLevel());
        result.setPublished(savedCourse.isPublished());
        result.setDuration(savedCourse.getDuration());
        result.setTutorId(savedCourse.getTutor().getTutorId());
        result.setRatingsCount(savedCourse.getRatingsCount());
        result.setAverageRating(savedCourse.getAverageRating());

        return result;
    }

    public List<CourseDTO> getCoursesBySubjectCategory(String categoryName) {
        Class_Categories category = null;

        for (Class_Categories c : Class_Categories.values()) {
            if (c.name().equalsIgnoreCase(categoryName)) {
                category = c;
                break;
            }
        }

        if (category == null) {
            throw new RuntimeException("Invalid subject category: " + categoryName);
        }

        List<Course> courses = courseRepository.findBySubject(category);

        return courses.stream().map(course -> {
            CourseDTO response = new CourseDTO();
            response.setCourseId(course.getCourseId());
            response.setCourseTitle(course.getCourseTitle());
            response.setDescription(course.getDescription());
            if (!(course.getTutor() == null)){
                response.setTutorId(course.getTutor().getTutorId());
            }
            response.setSubjectCategory(course.getSubject());
            response.setGradeLevel(course.getGradeLevel());
            response.setPublished(course.isPublished());
            response.setDuration(course.getDuration());
            response.setRatingsCount(course.getRatingsCount());
            response.setAverageRating(course.getAverageRating());
            response.setEnrollments(progressRepository.countByCourse_CourseId(course.getCourseId()));

            return response;
        }).toList();
    }


    public List<String> getAllCategoryNames() {
        return Class_Categories.getAllNames();
    }

    @Transactional
    public LessonDTO addLessonToCourse(String courseId, String tutorId, LessonDTO dto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getTutor().getTutorId().equals(tutorId)) {
            throw new BadRequestException("You are not authorized to add lessons to this course.");
        }

        Lesson newLesson = new Lesson();
        newLesson.setCourse(course);

        List<Lesson> existingLessons = lessonRepository.findByCourse_CourseIdOrderBySequenceNumberAsc(courseId);
        int nextSequence = existingLessons.isEmpty() ? 1 : existingLessons.get(existingLessons.size() - 1).getSequenceNumber() + 1;
        newLesson.setSequenceNumber(nextSequence);

        newLesson.setTitle(dto.getTitle());
        newLesson.setAbout(dto.getAbout());
        newLesson.setNotes(dto.getNotes());
        newLesson.setTranscript(dto.getTranscript());
        newLesson.setPassingScore(dto.getPassingScore());
        newLesson.setQuizNeeded(dto.isQuizNeeded());




        Lesson savedLesson = lessonRepository.save(newLesson);

        // Map to DTO
        LessonDTO ldto = new LessonDTO();
        ldto.setLessonId(savedLesson.getId());
        ldto.setTitle(savedLesson.getTitle());
        if (savedLesson.getAbout() != null) {
            ldto.setAbout(savedLesson.getAbout());
        }
        if (savedLesson.getNotes() != null){
            ldto.setNotes(savedLesson.getNotes());
        }
        if (savedLesson.getTranscript() != null){
            ldto.setTranscript(savedLesson.getTranscript());
        }
        ldto.setSequenceNumber(savedLesson.getSequenceNumber());
        ldto.setPassingScore(savedLesson.getPassingScore());
        ldto.setCourseId(savedLesson.getCourse().getCourseId());
        if (savedLesson.getVideo() != null){
            ldto.setVideoUrl(savedLesson.getVideo().getYoutubeVideoUrl());
            ldto.setYoutubeVideoId(savedLesson.getVideo().getYoutubeVideoId());
        }
        if (savedLesson.getQuizId() != null) {
            ldto.setQuizId(savedLesson.getQuizId());
        }

        return dto;
    }

    @Transactional
    public LessonDTO updateLesson(String lessonId, String tutorId, LessonDTO dto) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        if (!lesson.getCourse().getTutor().getTutorId().equals(tutorId)) {
            throw new BadRequestException("You are not authorized to add lessons to this course.");
        }

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));



        // Optionally set default title/content if needed
        lesson.setTitle(dto.getTitle());
        lesson.setAbout(dto.getAbout());
        lesson.setNotes(dto.getNotes());
        lesson.setTranscript(dto.getTranscript());
        lesson.setPassingScore(dto.getPassingScore());
        lesson.setQuizNeeded(dto.isQuizNeeded());


        if (videoRepository.findByYoutubeVideoId(dto.getYoutubeVideoId()).isEmpty()){
            LectureVideo lectureVideo = new LectureVideo();
            lectureVideo.setYoutubeVideoId(dto.getYoutubeVideoId());
            lectureVideo.setYoutubeVideoUrl("https://www.youtube.com/watch?v=" + dto.getYoutubeVideoId()); // <-- Save full URL
            lectureVideo.setTitle(dto.getTitle());
            lectureVideo.setDescription(dto.getTitle() + " - Video");
            lectureVideo.setTutor(tutor);
            lectureVideo.setCourse(lesson.getCourse());
            lectureVideo.setSubject(lesson.getCourse().getSubject());

            LectureVideo savedVideo = videoRepository.save(lectureVideo);
            lesson.setVideo(savedVideo);
        }


        Lesson savedLesson = lessonRepository.save(lesson);

        // Map to DTO
        LessonDTO ldto = new LessonDTO();
        ldto.setLessonId(savedLesson.getId());
        ldto.setTitle(savedLesson.getTitle());
        if (savedLesson.getAbout() != null) {
            ldto.setAbout(savedLesson.getAbout());
        }
        if (savedLesson.getNotes() != null){
            ldto.setNotes(savedLesson.getNotes());
        }
        if (savedLesson.getTranscript() != null){
            ldto.setTranscript(savedLesson.getTranscript());
        }
        ldto.setSequenceNumber(savedLesson.getSequenceNumber());
        ldto.setPassingScore(savedLesson.getPassingScore());
        ldto.setCourseId(savedLesson.getCourse().getCourseId());
        if (savedLesson.getVideo() != null){
            ldto.setVideoUrl(savedLesson.getVideo().getYoutubeVideoUrl());
            ldto.setYoutubeVideoId(savedLesson.getVideo().getYoutubeVideoId());
        }
        if (savedLesson.getQuizId() != null) {
            ldto.setQuizId(savedLesson.getQuizId());
        }
        ldto.setResourceFiles(getLessonResourceFiles(lesson.getId()));

        return ldto;
    }





    public LessonDTO fetchLessonForStudent(String studentId, String lessonId, HttpServletRequest request) {
        String token = Utils.getToken(request);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        UserDto student = userRepository.findById(studentId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", studentId)))
                .block();

        Course course = lesson.getCourse();

        // Get student's course progress
        StudentCourseProgress progress = progressRepository
                .findByUserIdAndCourse_CourseId(studentId, course.getCourseId())
                .orElseGet(() -> {
                    // Create a new progress entry
                    StudentCourseProgress newProgress = new StudentCourseProgress();
                    newProgress.setUserId(studentId);
                    assert student != null;
                    newProgress.setUsername(student.getUsername()); // if available
                    newProgress.setCourse(course);
                    newProgress.setCurrentLessonSequence(1); // starting from first lesson
                    newProgress.setProgressPercentage(0.0);
                    newProgress.setCompleted(false);
                    newProgress.setCompletedAt(null);
                    newProgress.setLessonProgressList(new ArrayList<>());

                    return progressRepository.save(newProgress);
                });

        // Prevent access if the lesson is beyond current sequence
        if (lesson.getSequenceNumber() > progress.getCurrentLessonSequence()) {
            throw new BadRequestException("You must complete previous lessons before accessing this one.");
        }

        LessonDTO dto = new LessonDTO();
        dto.setLessonId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setAbout(lesson.getAbout());
        dto.setNotes(lesson.getNotes());
        dto.setTranscript(lesson.getTranscript());
        if (lesson.getVideo() != null){
            dto.setYoutubeVideoId(lesson.getVideo().getYoutubeVideoId());
            dto.setVideoUrl(lesson.getVideo().getYoutubeVideoUrl());
        }
        dto.setSequenceNumber(lesson.getSequenceNumber());
        dto.setPassingScore(lesson.getPassingScore());
        dto.setCourseId(course.getCourseId());
        dto.setQuizNeeded(lesson.isQuizNeeded());
        dto.setResourceFiles(getLessonResourceFiles(lesson.getId()));


        StudentQuizDTO quiz;
        if (lesson.getQuizId() != null && !lesson.getQuizId().isBlank()) {

            quiz = webClient.get()
                    .uri("/quizzes/" + lesson.getQuizId() + "/student")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(StudentQuizDTO.class)
                    .block();
            dto.setQuiz(quiz);
            dto.setQuizId(lesson.getQuizId());

        }




        return dto;
    }

    public LessonDTO fetchLessonForTutor(String lessonId, String tutorId, HttpServletRequest request) {
        String token = Utils.getToken(request);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        if (!Objects.equals(tutorId, lesson.getCourse().getTutor().getTutorId())){
            throw new BadRequestException("You are not authorized to fetch lessons from this course.");
        }

        Course course = lesson.getCourse();
        LessonDTO dto = new LessonDTO();
        dto.setLessonId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setAbout(lesson.getAbout());
        dto.setNotes(lesson.getNotes());
        dto.setTranscript(lesson.getTranscript());
        if (lesson.getVideo() != null){
            dto.setYoutubeVideoId(lesson.getVideo().getYoutubeVideoId());
            dto.setVideoUrl(lesson.getVideo().getYoutubeVideoUrl());
        }
        dto.setSequenceNumber(lesson.getSequenceNumber());
        dto.setPassingScore(lesson.getPassingScore());
        dto.setCourseId(course.getCourseId());
        dto.setQuizNeeded(lesson.isQuizNeeded());
        dto.setResourceFiles(getLessonResourceFiles(lesson.getId()));


        if (lesson.getQuizId() != null && !lesson.getQuizId().isBlank()) {
            TeacherQuizDTO quiz = webClient.get()
                    .uri("/quizzes/" + lesson.getQuizId() + "/teacher")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(TeacherQuizDTO.class)
                    .block();
            dto.setQuizId(lesson.getQuizId());
            dto.setQuiz(quiz);
        }


        return dto;
    }

    public String addQuizToLesson(QuizCreationRequest request, HttpServletRequest servRequest) {
        String token = Utils.getToken(servRequest);

        // Basic validation
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Quiz title is required.");
        }

        if (request.getCourseId() == null || request.getCourseId().isBlank()) {
            throw new BadRequestException("Course ID is required.");
        }

        if (request.getLessonId() == null || request.getLessonId().isBlank()) {
            throw new BadRequestException("Lesson ID is required.");
        }

        if (request.getTutorId() == null || request.getTutorId().isBlank()) {
            throw new BadRequestException("Tutor ID is required.");
        }

        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new BadRequestException("At least one quiz question is required.");
        }

        // Confirm tutor exists
        if (!tutorRepository.existsById(request.getTutorId())) {
            throw new ResourceNotFoundException("Tutor", request.getTutorId());
        }

        if (!courseRepository.existsById(request.getCourseId())) {
            throw new ResourceNotFoundException("Course", request.getCourseId());
        }

        // Confirm lesson exists
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", request.getLessonId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));

        if (lesson.getCourse() != course){
            throw new BadRequestException("Lesson has be in that course");
        }

        // Create the quiz in the Question microservice
        String quizId = createQuiz(request, token);

        // Assign quizId to the lesson and save
        lesson.setQuizId(quizId);
        lesson.setQuizNeeded(true);
        lesson.setQuestionCount(request.getQuestions().size());
        lesson.setQuizTimeLimit(request.getTimeLimit());
        lesson.setPassingScore(request.getPassScore());
        lesson.setQuizNeeded(true);
        lessonRepository.save(lesson);

        return quizId;
    }

    @Transactional
    public TeacherQuizDTO updateQuiz(String lessonId, String quizId, QuizCreationRequest dto, HttpServletRequest request) {
        String token = Utils.getToken(request);
        if (lessonId.isEmpty()){
            throw new BadRequestException("Lesson Id is Empty");
        }

        if (quizId.isEmpty()){
            throw new BadRequestException("Quiz Id is Empty");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        if (!lesson.getCourse().getTutor().getTutorId().equals(dto.getTutorId())) {
            throw new BadRequestException("You are not authorized to add lessons to this course.");
        }

        if (lesson.getQuizId().isEmpty() || lesson.getQuizId().isBlank()){
            throw new BadRequestException("Lesson does not have quiz");
        }

        if (!lesson.getQuizId().equalsIgnoreCase(quizId)){
            throw new BadRequestException("Incorrect Quiz Id");
        }

        TeacherQuizDTO response = webClient.put()
                .uri("/quizzes/" + quizId)
                .headers(headers -> headers.setBearerAuth(token)) // <-- Add Bearer token
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(TeacherQuizDTO.class)
                .block();

        lesson.setQuestionCount(dto.getQuestions().size());
        lesson.setQuizTimeLimit(dto.getTimeLimit());
        lesson.setPassingScore(dto.getPassScore());

        lessonRepository.save(lesson);

        return response;
    }

    private String createQuiz(QuizCreationRequest request, String token) {
        try {
            QuizResponseDTO response = webClient.post()
                    .uri("/quizzes")
                    .headers(headers -> headers.setBearerAuth(token))
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(QuizResponseDTO.class)
                    .block();

            if (response == null || response.getId() == null) {
                throw new QuizCreationException("Quiz creation failed: No ID returned from question service.");
            }

            return response.getId();

        } catch (WebClientResponseException e) {
            // HTTP error returned from remote service
            throw new QuizCreationException("Quiz service error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);

        } catch (WebClientRequestException e) {
            // I/O error, timeout, service unavailable
            throw new QuizCreationException("Failed to connect to question service: " + e.getMessage(), e);

        } catch (Exception e) {
            // Catch-all for unexpected errors
            throw new QuizCreationException("Unexpected error while creating quiz: " + e.getMessage(), e);
        }
    }

    public QuizResponseDTO uploadQuizFromCSV(MultipartFile file, String tutorId, String courseId, String lessonId, String title, HttpServletRequest request) {
        String token = Utils.getToken(request);
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Quiz title is required.");
        }

        if (courseId == null || courseId.isBlank()) {
            throw new BadRequestException("Course ID is required.");
        }

        if (lessonId == null || lessonId.isBlank()) {
            throw new BadRequestException("Lesson ID is required.");
        }

        if (tutorId == null || tutorId.isBlank()) {
            throw new BadRequestException("Tutor ID is required.");
        }

        // Confirm tutor exists
        if (!tutorRepository.existsById(tutorId)) {
            throw new ResourceNotFoundException("Tutor", tutorId);
        }

        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        // Confirm lesson exists
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        String quizId;
        QuizResponseDTO quizResponseDTO;
        try {
            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("file", new FileSystemResource(convertMultiPartToFile(file)));
            formData.add("tutorId", tutorId);
            formData.add("title", title);

            quizResponseDTO =  webClient.post()
                    .uri( "/quizzes/CSV")
                    .headers(headers -> headers.setBearerAuth(token)) // <-- Add Bearer token
                    .contentType(org.springframework.http.MediaType.valueOf(MediaType.MULTIPART_FORM_DATA))
                    .body(BodyInserters.fromMultipartData(formData))
                    .retrieve()
                    .bodyToMono(QuizResponseDTO.class)
                    .block();

            assert quizResponseDTO != null;
            quizId = quizResponseDTO.getId();

        } catch (WebClientResponseException e) {
            // HTTP error returned from remote service
            throw new QuizCreationException("Quiz service error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (WebClientRequestException e) {
            // I/O error, timeout, service unavailable
            throw new QuizCreationException("Failed to connect to quiz service: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch-all for unexpected errors
            throw new QuizCreationException("Unexpected error while uploading CSV quiz: " + e.getMessage(), e);
        }

        // Assign quizId to the lesson and save
        lesson.setQuizId(quizId);
        lesson.setQuizNeeded(true);
        lessonRepository.save(lesson);

        return quizResponseDTO;
    }

    public QuizResponseDTO uploadMockExamFromCSV(MultipartFile file, String tutorId, String title, HttpServletRequest request) {
        String token = Utils.getToken(request);

        if (title == null || title.isBlank()) {
            throw new BadRequestException("Quiz title is required.");
        }


        if (tutorId == null || tutorId.isBlank()) {
            throw new BadRequestException("Tutor ID is required.");
        }

        // Confirm tutor exists
        if (!tutorRepository.existsById(tutorId)) {
            throw new ResourceNotFoundException("Tutor", tutorId);
        }



        String mockExamId;
        QuizResponseDTO quizResponseDTO;
        try {
            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("file", new FileSystemResource(convertMultiPartToFile(file)));
            formData.add("tutorId", tutorId);
            formData.add("title", title);

            quizResponseDTO =  webClient.post()
                    .uri("/mock-exams/upload")
                    .contentType(org.springframework.http.MediaType.valueOf(MediaType.MULTIPART_FORM_DATA))
                    .headers(headers -> headers.setBearerAuth(token))
                    .body(BodyInserters.fromMultipartData(formData))
                    .retrieve()
                    .bodyToMono(QuizResponseDTO.class)
                    .block();

            assert quizResponseDTO != null;
            mockExamId = quizResponseDTO.getId();

        } catch (WebClientResponseException e) {
            // HTTP error returned from remote service
            throw new QuizCreationException("Mock Exam service error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (WebClientRequestException e) {
            // I/O error, timeout, service unavailable
            throw new QuizCreationException("Failed to connect to question service: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch-all for unexpected errors
            throw new QuizCreationException("Unexpected error while uploading CSV exam: " + e.getMessage(), e);
        }
        MockExam mockExam = new MockExam();
        mockExam.setMockExamId(mockExamId);
        mockExam.setQuestionCount(quizResponseDTO.getTotalQuestions());
        mockExam.setTitle(title);

        mockExamRepository.save(mockExam);

        return quizResponseDTO;
    }



    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertedFile = File.createTempFile("temp", null);
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }

    @Transactional
    public StudentQuizResultDTO submitQuizAnswers(QuizSubmitRequest request, HttpServletRequest servletRequest) {
        String token = Utils.getToken(servletRequest);

        if (request == null || request.getQuizId().isBlank()) {
            throw new QuizCreationException("Quiz ID must not be null or empty.");
        }

        if (request.getStudentId() == null || request.getStudentId().isBlank()){
            throw new QuizCreationException("Student ID must not be null or empty.");
        }

        if (request.getAnswers() == null || request.getAnswers().isEmpty()){
            throw new QuizCreationException("Quiz must contain at least one question/answer.");
        }

        String quizId = request.getQuizId();

        // 1. Submit the quiz via WebClient
        StudentQuizResultDTO result;
        try {
            result = webClient.post()
                    .uri("/quizzes/submit")
                    .headers(headers -> headers.setBearerAuth(token))
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(StudentQuizResultDTO.class)
                    .block();

            if (result == null || result.getQuizId() == null) {
                throw new QuizCreationException("Quiz submission failed: No result returned from question service.");
            }
        } catch (WebClientResponseException e) {
            throw new QuizCreationException("Quiz service error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (WebClientRequestException e) {
            throw new QuizCreationException("Failed to connect to question service: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new QuizCreationException("Unexpected error while submitting quiz answers: " + e.getMessage(), e);
        }

        // 2. Find the lesson and related course
        Lesson lesson = lessonRepository.findByQuizId(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found for quizId: " + quizId));

        Course course = lesson.getCourse();

        // 3. Find StudentCourseProgress
        StudentCourseProgress courseProgress = progressRepository
                .findByUserIdAndCourse_CourseId(request.getStudentId(), course.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found for user and course"));

        // 4. Find StudentLessonProgress
        StudentLessonProgress lessonProgress = courseProgress.getLessonProgressList().stream()
                .filter(lp -> lp.getLesson().getId().equals(lesson.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Lesson progress not found"));

        // 5. Update the lesson progress
        lessonProgress.setQuizScore(result.getScorePercentage());
        lessonProgress.setPassedQuiz(result.getScorePercentage() >= lesson.getPassingScore());
        lessonProgress.setCompleted(true);
        lessonProgress.setCompletedAt(java.time.LocalDateTime.now());

        if (lessonProgress.isPassedQuiz()) {
            int nextSeq = lesson.getSequenceNumber() + 1;

            Optional<Lesson> nextLessonOpt = lessonRepository.findByCourseAndSequenceNumber(course, nextSeq);

            if (nextLessonOpt.isPresent()) {
                Lesson nextLesson = nextLessonOpt.get();
                boolean alreadyExists = courseProgress.getLessonProgressList().stream()
                        .anyMatch(lp -> lp.getLesson().getId().equals(nextLesson.getId()));
                if (!alreadyExists) {
                    StudentLessonProgress nextProgress = new StudentLessonProgress();
                    nextProgress.setCourseProgress(courseProgress);
                    nextProgress.setLesson(nextLesson);
                    nextProgress.setCompleted(false);
                    nextProgress.setPassedQuiz(false);

                    courseProgress.getLessonProgressList().add(nextProgress);
                }

                courseProgress.setCurrentLessonSequence(nextSeq);
            } else {
                // No next lesson — mark course as completed
                courseProgress.setCompleted(true);
                courseProgress.setCompletedAt(LocalDateTime.now());
            }
        }


        long totalLessons = lessonRepository.countByCourse(course);
        long completedLessons = courseProgress.getLessonProgressList().stream().filter(StudentLessonProgress::isCompleted).count();
        double progressPercentage = ((double) completedLessons / totalLessons) * 100;
        courseProgress.setProgressPercentage(progressPercentage);

        progressRepository.save(courseProgress);

        return result;
    }


    @Transactional
    public StudentMockExamResultDTO submitExamAnswers(MockExamSubmitRequest request, HttpServletRequest servletRequest) {
        String token = Utils.getToken(servletRequest);

        if (request == null || request.getMockExamId().isBlank()) {
            throw new QuizCreationException("Quiz ID must not be null or empty.");
        }

        if (request.getStudentId() == null || request.getStudentId().isBlank()){
            throw new QuizCreationException("Student ID must not be null or empty.");
        }

        if (request.getAnswers() == null || request.getAnswers().isEmpty()){
            throw new QuizCreationException("Quiz must contain at least one question/answer.");
        }

        // 1. Submit the quiz via WebClient
        StudentMockExamResultDTO result;
        try {
            result = webClient.post()
                    .uri("/mock-exams/submit")
                    .headers(headers -> headers.setBearerAuth(token)) // <-- Add Bearer token
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(StudentMockExamResultDTO.class)
                    .block();

            if (result == null || result.getMockExamId() == null) {
                throw new QuizCreationException("Exam submission failed: No result returned from question service.");
            }

            // Retrieve MockExam and Student entities
            MockExam mockExam = mockExamRepository.findById(request.getMockExamId())
                    .orElseThrow(() -> new QuizCreationException("MockExam not found with ID: " + request.getMockExamId()));

            // Count previous attempts
            int attemptCount = mockExamStudentProgressRepository.countByMockExamAndStudentId(mockExam, request.getStudentId());

            // Save progress
            MockExamStudentProgress progress = new MockExamStudentProgress();
            progress.setMockExam(mockExam);
            progress.setStudentId(request.getStudentId());
            progress.setAttemptNumber(attemptCount + 1);
            progress.setScore(result.getScorePercentage());
            progress.setAttemptDate(LocalDateTime.now());

            mockExamStudentProgressRepository.save(progress);

        } catch (WebClientResponseException e) {
            throw new QuizCreationException("Exam service error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (WebClientRequestException e) {
            throw new QuizCreationException("Failed to connect to question service: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new QuizCreationException("Unexpected error while submitting quiz answers: " + e.getMessage(), e);
        }

        return result;

    }


    public String updateCourse(CourseDTO courseDTO) {
        Course course = courseRepository.findById(courseDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseDTO.getCourseId()));

        Tutor requestingTutor = tutorRepository.findById(courseDTO.getTutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", courseDTO.getTutorId()));

        if (!course.getTutor().getTutorId().equals(requestingTutor.getTutorId())) {
            throw new BadRequestException("You are not authorized to update this course.");
        }

        course.setCourseTitle(courseDTO.getCourseTitle());
        course.setDescription(courseDTO.getDescription());
        course.setSubject(courseDTO.getSubjectCategory());
        course.setGradeLevel(courseDTO.getGradeLevel());
        course.setPublished(courseDTO.isPublished());
        course.setDuration(courseDTO.getDuration());

        courseRepository.save(course);
        return "Course updated successfully.";
    }


    public void deleteCourse(String courseId, String tutorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        // Ensure the tutor exists
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", tutorId));

        // Check ownership
        if (!course.getTutor().getTutorId().equals(tutor.getTutorId())) {
            throw new BadRequestException("You are not authorized to delete this course.");
        }

        courseRepository.delete(course);
    }


    public CourseDisplayDTO getCourseById(String courseId, String userId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        // Fetch course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Fetch user
        UserDto user = userRepository.findById(userId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", userId)))
                .block();

        // Check if user's category matches the course's category
        assert user != null;
        if (!user.getClassCategory().equals(course.getSubject())) {
            throw new BadRequestException("User is not permitted to access this course.");
        }

        // Map and return course display info
        return mapCourseToDisplayDTO(course, token);
    }



    public List<CourseDTO> getCoursesByTutor(String tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", tutorId));

        List<Course> courses = courseRepository.findByTutor(tutor);
        return courses.stream().map(this::mapCourseToDTO).toList();
    }


    @Transactional
    public String rateCourse(RatingDTO ratingDTO, HttpServletRequest request) {
        String token = Utils.getToken(request);

        if (ratingDTO.getRating() < 1 || ratingDTO.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5.");
        }

        UserDto student = userRepository.findById(ratingDTO.getUserId(), token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", ratingDTO.getUserId())))
                .block();

        Course course = courseRepository.findById(ratingDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", ratingDTO.getCourseId()));

        boolean alreadyRated = courseRatingRepository.existsByStudentIdAndCourse(student.getUserId(), course);
        if (alreadyRated) {
            throw new RatingException("You have already rated this course.");
        }

        CourseRating courseRating = new CourseRating();
        courseRating.setStudentId(student.getUserId());
        courseRating.setStudentUsername(student.getUsername());
        courseRating.setCourse(course);
        courseRating.setTime(LocalDateTime.now());

        course.setRatingsCount(course.getRatingsCount() + 1);
        course.setAverageRating(calculateCourseRating(course.getCourseId()));

        courseRepository.save(course);
        courseRatingRepository.save(courseRating);

        return "Course rated successfully.";
    }

    public List<RatingDTO> getRatingsForCourse(String courseId) {
        if (courseId.isBlank()){
            throw new BadRequestException("Course Id is empty");
        }
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        // Fetch course ratings
        List<CourseRating> courseRatings = courseRatingRepository.findByCourse_CourseId(courseId);

        // Convert CourseRating entities to RatingDTOs
        List<RatingDTO> ratingDTOs = courseRatings.stream()
                .map(courseRating -> new RatingDTO(
                        courseRating.getStudentId(),
                        null,
                        courseRating.getStudentUsername(),
                        courseRating.getCourse().getCourseId(),
                        courseRating.getRating(),
                        courseRating.getFeedback(),
                        courseRating.getTime()
                ))
                .collect(Collectors.toList());

        return ratingDTOs;
    }


    public double calculateCourseRating(String courseId) {
        List<RatingDTO> ratings = getRatingsForCourse(courseId);
        return ratings.stream()
                .mapToInt(RatingDTO::getRating)
                .average()
                .orElse(0.0);
    }

//    public SessionResponseDTO scheduleLiveGroupSession(LiveStreamDTO liveStreamDTO){
//        return sessionService.scheduleCourseSession(liveStreamDTO);
//    }
//
//    public SessionResponseDTO startGroupSessionLiveStream(LiveStreamDTO liveStreamDTO){
//        sessionService.
//    }





    public List<CourseLiveStreamDTO> getLiveStreamsForCourse(String courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        List<LiveStream> streams = liveStreamRepository.findByCourse_CourseIdOrderByScheduledTimeDesc(courseId);

        return streams.stream().map(stream -> {
            CourseLiveStreamDTO dto = new CourseLiveStreamDTO();
            dto.setId(stream.getId());
            dto.setYoutubeBroadcastId(stream.getYoutubeBroadcastId());
            dto.setYoutubeStreamId(stream.getYoutubeStreamId());
            dto.setIngestionAddress(stream.getIngestionAddress());
            dto.setStreamKey(stream.getStreamKey());
            dto.setWatchUrl(stream.getWatchUrl());
            dto.setTitle(stream.getTitle());
            dto.setDescription(stream.getDescription());
            dto.setScheduledTime(stream.getScheduledTime());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<SubjectDemandDTO> getSubjectDemand(){
        return new ArrayList<>();
    }




    public CourseDTO mapCourseToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setCourseId(course.getCourseId());
        dto.setCourseTitle(course.getCourseTitle());
        dto.setDescription(course.getDescription());
        dto.setSubjectCategory(course.getSubject());
        dto.setGradeLevel(course.getGradeLevel());
        dto.setPublished(course.isPublished());
        dto.setDuration(course.getDuration());
        dto.setTutorId(course.getTutor().getTutorId());
        dto.setRatingsCount(course.getRatingsCount());
        dto.setAverageRating(course.getAverageRating());
        dto.setLessonCount(course.getLessons().size());
        dto.setEnrollments(progressRepository.countByCourse_CourseId(course.getCourseId()));
        dto.setUploadedDate(course.getCreatedAt().toLocalDate());



        return dto;
    }

    private CourseDisplayDTO mapCourseToDisplayDTO(Course course, String token) {
        CourseDisplayDTO dto = new CourseDisplayDTO();

        dto.setCourseId(course.getCourseId());
        dto.setCourseTitle(course.getCourseTitle());
        dto.setDescription(course.getDescription());
        dto.setTutorId(course.getTutor().getTutorId());
        dto.setSubjectCategory(course.getSubject());
        dto.setGradeLevel(course.getGradeLevel());
        dto.setPublished(course.isPublished());
        dto.setDuration(course.getDuration());
        dto.setRatingsCount(course.getRatingsCount());
        dto.setAverageRating(course.getAverageRating());
        dto.setUploadedDate(course.getCreatedAt().toLocalDate());

        // Lessons as LessonIntroDTOs
        List<LessonIntroDTO> lessonDTOs = lessonRepository.findByCourse_CourseIdOrderBySequenceNumberAsc(course.getCourseId())
                .stream()
                .map(lesson -> new LessonIntroDTO(
                        lesson.getId(),
                        lesson.getTitle(),
                        lesson.getSequenceNumber(),
                        lesson.getAbout(),
                        lesson.getDuration()
                ))
                .toList();
        dto.setLessons(lessonDTOs);
        dto.setLessonCount(lessonDTOs.size());

        // Student count (assumes course.getEnrolledUsers() is up-to-date)
        dto.setStudentCount(course.getEnrolledUsers() != null ? course.getEnrolledUsers().size() : 0);

        // Tutor display (name or full name — adjust based on Tutor model)
        dto.setTutor(tutorService.mapToDTO(course.getTutor(), token).getFullName()); // Assuming getFullName() exists

        // Ratings (stubbed or retrieved elsewhere — update if needed)
        dto.setRatings(getRatingsForCourse(course.getCourseId())); // If not implemented yet

        // Last updated timestamp from Auditable (assuming getLastModifiedDate())
        if (course.getUpdatedAt() != null) {
            dto.setLastUpdated(course.getUpdatedAt().toString());
        } else {
            dto.setLastUpdated("N/A");
        }

        // Optional: studentId if you want to personalize (can be passed in or skipped)
        dto.setStudentId(null);

        return dto;
    }


    public List<QuizResponseDTO> getAllMockExams() {
        return mockExamRepository.findAll()
                .stream()
                .map(mockExam -> {
                    QuizResponseDTO dto = new QuizResponseDTO();

                    // Decide which ID to expose
                    dto.setId(mockExam.getId());

                    dto.setTitle(mockExam.getTitle());
                    dto.setTotalQuestions(mockExam.getQuestionCount());


                    return dto;
                })
                .collect(Collectors.toList());
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

    private List<ResourceFileResponse> getLessonResourceFiles(String lessonId) {
        if (lessonId.isBlank() || lessonId.isEmpty()){
            throw new BadRequestException("Lesson Id is not provided");
        }


        return resourceDocumentRepository.findByLessonId(lessonId)
                .stream().map(x -> {
                    ResourceFileResponse response = new ResourceFileResponse();
                    response.setCourseId(x.getCourseId());
                    response.setTitle(x.getTitle());
                    response.setFileUrl(x.getFileUrl());
                    response.setId(x.getId());
                    response.setType(x.getType());
                    response.setTimeUploaded(x.getTimeCreated());

                    return response;
                })
                .collect(Collectors.toList());
    }

    public StudentQuizDTO getMockExamForStudent(String examId, HttpServletRequest request) {
        if (examId.isBlank() || examId.isEmpty()){
            throw new BadRequestException("Exam Id is empty");
        }
        String token = Utils.getToken(request);
        MockExam mockExam = mockExamRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("MockExam not found with id: " + examId));
        StudentQuizDTO exam;
        try {
             exam = webClient.get()
                    .uri("/mock-exams/" + examId + "/student")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(StudentQuizDTO.class)
                    .block();
        } catch (Exception e){
            throw new BadRequestException("Error retrieving exam: " + e.getMessage());
        }


        return exam;
    }

    public TeacherQuizDTO getMockExamByForTeacher(String examId, String tutorId,  HttpServletRequest request) {
        if (examId.isBlank() || examId.isEmpty()){
            throw new BadRequestException("Exam Id is empty");
        }
        if (tutorId.isBlank() || tutorId.isEmpty()){
            throw new BadRequestException("Tutor Id is empty");
        }
        String token = Utils.getToken(request);
        if (mockExamRepository.existsById(examId)){
            throw new BadRequestException("Exam does not exist with id: " + examId);
        }

        TeacherQuizDTO exam;
        try {
            exam = webClient.get()
                    .uri("/mock-exams/" + examId + "/teacher")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(TeacherQuizDTO.class)
                    .block();
        } catch (Exception e){
            throw new BadRequestException("Error retrieving exam: " + e.getMessage());
        }


        return exam;
    }


}
