package com.flexydemy.content.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.flexydemy.content.dto.*;
import com.flexydemy.content.enums.SessionType;
import com.flexydemy.content.enums.TutorStatus;
import com.flexydemy.content.exceptions.AuthorizationException;
import com.flexydemy.content.exceptions.BadRequestException;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.*;
import com.flexydemy.content.repository.*;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class AdminService {

    private final UserClient userRepository;
    private final TutorRepository tutorRepository;
    private final CourseRepository courseRepository;
    private final SessionRepository sessionRepository;
    private final FlashCardSetRepository flashCardSetRepository;
    private final MockExamRepository mockExamRepository;
    private final MockExamStudentProgressRepository mockExamStudentProgressRepository;
    private final StudentLessonProgressRepository studentLessonProgressRepository;
    private final TutorService tutorService;
    private final LessonRepository lessonRepository;
    private final FlashCardService flashCardService;
    private final FeedbackRepository feedbackRepository;
    private final CourseService courseService;
    private final VideoRepository videoRepository;
    private final ResourceDocumentRepository resourceDocumentRepository;
    private final Cloudinary cloudinary;
    private final WebClient webClient;

    public AdminService(
            UserClient userRepository,
            TutorRepository tutorRepository,
            CourseRepository courseRepository,
            SessionRepository sessionRepository,
            FlashCardSetRepository flashCardSetRepository,
            MockExamRepository mockExamRepository,
            MockExamStudentProgressRepository mockExamStudentProgressRepository,
            StudentLessonProgressRepository studentLessonProgressRepository,
            TutorService tutorService,
            LessonRepository lessonRepository,
            FlashCardService flashCardService,
            FeedbackRepository feedbackRepository,
            CourseService courseService,
            VideoRepository videoRepository,
            ResourceDocumentRepository resourceDocumentRepository,
            Cloudinary cloudinary,
            WebClient.Builder builder,
            @Value("${question-service.url}") String questionServiceUrl
    ) {
        this.userRepository = userRepository;
        this.tutorRepository = tutorRepository;
        this.courseRepository = courseRepository;
        this.sessionRepository = sessionRepository;
        this.flashCardSetRepository = flashCardSetRepository;
        this.mockExamRepository = mockExamRepository;
        this.mockExamStudentProgressRepository = mockExamStudentProgressRepository;
        this.studentLessonProgressRepository = studentLessonProgressRepository;
        this.tutorService = tutorService;
        this.lessonRepository = lessonRepository;
        this.flashCardService = flashCardService;
        this.feedbackRepository = feedbackRepository;
        this.courseService = courseService;
        this.videoRepository = videoRepository;
        this.resourceDocumentRepository = resourceDocumentRepository;
        this.cloudinary = cloudinary;
        this.webClient = builder.baseUrl(questionServiceUrl).build();
    }
    /**
     * Validates that a user exists and is an admin.
     */
    private UserDto validateAdmin(String adminId, String token) {
        UserDto user = userRepository.findById(adminId, token)
                .blockOptional()
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        if (user.getRole() == null || !user.getRole().contains(ProfileRole.ADMIN)) {
            throw new AuthorizationException("User is not authorized as an admin");
        }

        return user;
    }

    /**
     * Approve or flag tutor requests.
     */
    @Transactional
    public void approveTutorRequest(String adminId, String tutorId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        validateAdmin(adminId, token);

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with ID: " + tutorId));

        try {
            tutor.setStatus(TutorStatus.APPROVED);
            tutorRepository.save(tutor);

        } catch (Exception e) {
            throw new BadRequestException("Failed to update tutor status. Status must be: APPROVED, FLAGGED, or PENDING.");
        }
    }

    @Transactional
    public void denyTutorRequest(String adminId, String tutorId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        validateAdmin(adminId, token);

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with ID: " + tutorId));

        try {
            tutor.setStatus(TutorStatus.DENIED);
            tutorRepository.save(tutor);

        } catch (Exception e) {
            throw new BadRequestException("Failed to update tutor status. Status must be: APPROVED, FLAGGED, or PENDING.");
        }
    }

    @Transactional
    public void flagTutor(String adminId, String tutorId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        validateAdmin(adminId, token);

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with ID: " + tutorId));

        try {
            tutor.setStatus(TutorStatus.FLAGGED);
            tutorRepository.save(tutor);
        } catch (Exception e) {
            throw new BadRequestException("Failed to update tutor status. Status must be: APPROVED, FLAGGED, or PENDING.");
        }
    }

    /**
     * Return all registered tutors
     */
    @Transactional(readOnly = true)
    public List<TutorDTO> getAllTutors(String adminId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        UserDto user = validateAdmin(adminId, token);

        return tutorRepository.findAll()
                .stream()
                .map(tutor -> {
                    TutorDTO dto = new TutorDTO();
                    dto.setTutorId(tutor.getTutorId());
                    dto.setUserId(tutor.getUserId());
                    dto.setFullName(user.getFirstName() + " " + user.getLastName());
                    dto.setProfileVideoUrl(user.getProfileImageUrl());
                    dto.setWorkingDays(tutor.getWorkingDays());
                    dto.setAreaOfExpertise(tutor.getAreaOfExpertise());
                    dto.setBio(tutor.getBio());
                    dto.setQualifications(tutor.getQualifications());
                    dto.setVerified(tutor.isVerified());
                    dto.setTotalSubscribers(tutor.getTotalSubscribers());
                    dto.setRating(tutor.getRatingsCount());
                    dto.setJoinDate(tutor.getJoinDate());
                    dto.setSchools(tutor.getSchools());
                    dto.setWorkExperiences(tutor.getWorkExperiences());
                    dto.setSessionCount(sessionRepository.countByTutor_TutorId(tutor.getTutorId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TutorDTO> getAllPendingTutors(String adminId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        UserDto user = validateAdmin(adminId, token);

        return tutorRepository.findAll()
                .stream()
                .filter(tutor -> tutor.getStatus() == TutorStatus.PENDING)
                .map(tutor -> {
                    TutorDTO dto = new TutorDTO();
                    dto.setTutorId(tutor.getTutorId());
                    dto.setUserId(tutor.getUserId());
                    dto.setFullName(user.getFirstName() + " " + user.getLastName());
                    dto.setProfileVideoUrl(user.getProfileImageUrl());
                    dto.setWorkingDays(tutor.getWorkingDays());
                    dto.setAreaOfExpertise(tutor.getAreaOfExpertise());
                    dto.setBio(tutor.getBio());
                    dto.setQualifications(tutor.getQualifications());
                    dto.setVerified(tutor.isVerified());
                    dto.setTotalSubscribers(tutor.getTotalSubscribers());
                    dto.setRating(tutor.getRatingsCount());
                    dto.setJoinDate(tutor.getJoinDate());
                    dto.setSchools(tutor.getSchools());
                    dto.setWorkExperiences(tutor.getWorkExperiences());
                    dto.setSessionCount(sessionRepository.countByTutor_TutorId(tutor.getTutorId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Return all students
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents(String adminId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        validateAdmin(adminId, token);
        return userRepository.findAllStudents(adminId, token);
    }

    @Transactional(readOnly = true)
    public UserDto getStudentInfo(String adminId, String studentId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        validateAdmin(adminId, token);

        return userRepository.findById(adminId, token)
                .blockOptional()
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));
    }

    /**
     * Return all courses
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses(String adminId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        validateAdmin(adminId, token);

        List<CourseDTO> courses = courseRepository.findAll()
                .stream()
                .map(courseService::mapCourseToDTO)
                .collect(Collectors.toList());
        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("No courses found.");
        }

        return courses;
    }

    public AdminDashboardDTO getAdminDashboard(String adminId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        validateAdmin(adminId, token);

        AdminDashboardDTO dto = new AdminDashboardDTO();
        dto.setUserId(adminId);

        // Student & Teacher count
        //dto.setStudents(userRepository.countByRole(Role.STUDENT, token));
        //dto.setTeachers(userRepository.countByRole(Role.TUTOR, token));

        // One-on-one session count
        dto.setOneOnOneSessions(sessionRepository.countBySessionType(SessionType.ONE_ON_ONE));

        // Feedback count
        dto.setFeedbacks(feedbackRepository.countByReviewedFalse());

        // Earnings breakdown by month and exam
        //dto.setEarningsByMonth(courseRepository.getMonthlyEarningsBreakdown());

        // Subject demand
        dto.setSubjectDemand(courseService.getSubjectDemand());

        // Session list
        dto.setSessions(sessionRepository.findAll().stream().map(session -> {
            //String tutorName = session.getTutor() != null ? session.getTutor()() : "N/A";
            String courseTitle = session.getCourseId();
            int progress = session.getStudentIds().size() * 10; // Replace with real progress
            String status = session.getSessionStatus().name();

            return new AdminSessionDisplayDTO(
                    "tutorName",
                    "Students: " + session.getStudentIds().size(),
                    courseTitle,
                    progress,
                    status,
                    session.getStartDateTime()
            );
        }).collect(Collectors.toList()));

        // Top courses
        //dto.setTopCourses(courseRepository.findTopCoursesWithStats());

        // Demand per class category
        //dto.setCategoryDemand(courseRepository.countStudentsPerCategory());

        return dto;
    }

    public FlashCardSetResponseDTO createFlashCardSet(FlashCardSetRequestDTO dto, String adminId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        UserDto admin = validateAdmin(adminId, token);

        return flashCardService.createFlashCardSet(dto, admin);
    }

    // Create a flashcard in a set
    public FlashCardResponseDTO addFlashCard(String setId, FlashCardItemDTO dto, String adminId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        UserDto admin = validateAdmin(adminId, token);

        return flashCardService.addFlashCardToSet(setId, null, dto, admin);
    }

    // Update a flashcard
    public FlashCardResponseDTO updateFlashCard(String flashCardId, FlashCardItemDTO dto, String adminId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        UserDto admin = validateAdmin(adminId, token);

        return flashCardService.updateFlashCard(flashCardId, dto, null, admin);
    }

    // Delete a single flashcard
    public void deleteFlashCard(String flashCardId, String adminId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        UserDto admin = validateAdmin(adminId, token);

        flashCardService.deleteFlashCard(flashCardId, null, admin);
    }

    // Delete an entire flashcard set
    public void deleteFlashCardSet(String flashCardSetId, String adminId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        UserDto admin = validateAdmin(adminId, token);

        flashCardService.deleteFlashCardSet(flashCardSetId, null, admin);
    }

    public String replaceCourseTutor(String tutorId, String adminId, String courseId, HttpServletRequest request) {

        if (tutorId == null || tutorId.isEmpty()) {
            throw new BadRequestException("Tutor Id is required.");
        }

        if (courseId == null || courseId.isEmpty()) {
            throw new BadRequestException("Course Id is required.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));


        course.setTutor(tutor);

        courseRepository.save(course);

        return "Tutor Successfully Replaced";


    }

    public List<AssessmentsDTO> getAllAssessments(String adminId, String type, HttpServletRequest request) {

        List<AssessmentsDTO> assessments = new ArrayList<>();

        if (type == null || type.equalsIgnoreCase("flashcard")){
            // 1️⃣ FlashCard Sets
            flashCardSetRepository.findAll().forEach(flashCardSet -> {
                AssessmentsDTO dto = new AssessmentsDTO();
                dto.setTitle(flashCardSet.getTitle());
                dto.setType("FlashCard");
                dto.setQuestions(flashCardSet.getFlashCards() != null ? flashCardSet.getFlashCards().size() : 0);
                dto.setTimeLimit(0); // Not applicable
                dto.setSubject(flashCardSet.getSubject() != null ? Collections.singletonList(flashCardSet.getSubject().getName()) : new ArrayList<>());
                dto.setAverageScore(0); // Optional, can calculate if needed
                dto.setStatus("Active"); // Or flashCardSet.getStatus() if exists
                dto.setAttempts(0); // Not applicable
                assessments.add(dto);
            });
        }

        if (type == null || type.equalsIgnoreCase("mockexam")){
            // 2️⃣ Mock Exams
            mockExamRepository.findAll().forEach(mockExam -> {
                AssessmentsDTO dto = new AssessmentsDTO();
                dto.setTitle(mockExam.getTitle());
                dto.setType("MockExam");
                dto.setQuestions(mockExam.getQuestionCount());
                dto.setTimeLimit(mockExam.getTimeLimit()); // Optional: set time limit if you have
                dto.setSubject(mockExam.getSubject());
                dto.setAverageScore(mockExamStudentProgressRepository
                        .findAll()
                        .stream()
                        .filter(p -> p.getMockExam().getId().equals(mockExam.getId()))
                        .mapToDouble(MockExamStudentProgress::getScore)
                        .average()
                        .orElse(0));
                dto.setStatus("Active"); // Optional
                dto.setAttempts(mockExamStudentProgressRepository
                        .findAll()
                        .stream()
                        .filter(p -> p.getMockExam().getId().equals(mockExam.getId()))
                        .mapToInt(MockExamStudentProgress::getAttemptNumber)
                        .sum());
                assessments.add(dto);
            });
        }


        if (type == null || type.equalsIgnoreCase("quiz")){
            // 3️⃣ Quizzes
            studentLessonProgressRepository.findAll().forEach(progress -> {
                AssessmentsDTO dto = new AssessmentsDTO();
                dto.setTitle(progress.getLesson() != null ? progress.getLesson().getTitle() + " Quiz" : "Lesson Quiz");
                dto.setType("Quiz");
                dto.setQuestions(progress.getLesson() != null
                        ? progress.getLesson().getQuestionCount()
                        : 0);
                dto.setTimeLimit(progress.getLesson() != null
                        ? progress.getLesson().getQuizTimeLimit()
                        : 0);
                dto.setSubject(progress.getLesson() != null
                        ? Collections.singletonList(progress.getLesson().getCourse().getSubject().getName()) : new ArrayList<>());
                dto.setAverageScore(progress.isCompleted() ? progress.getQuizScore() : 0);
                dto.setStatus(progress.isCompleted() ? "Completed" : "Pending");
                dto.setAttempts(progress.isCompleted() ? 1 : 0);
                assessments.add(dto);
            });
        }


        return assessments;
    }

    public CourseDTO createCourse(CourseDTO dto, String adminId) {
        return courseService.createCourse(dto);
    }

    public String updateCourse(CourseDTO dto, String adminId) {
        return courseService.updateCourse(dto);
    }

    public MaterialsDTO getAllMaterials(String adminId, String type, HttpServletRequest request) {
        // Initialize the DTO to hold the result
        MaterialsDTO materialsDTO = new MaterialsDTO();

        // Fetch all materials for the given adminId (adjust with your actual data source)
        List<ResourceDocument> allMaterials = resourceDocumentRepository.findAll();

        // Check if type is provided and it's "videos"
        if ("videos".equalsIgnoreCase(type)) {
            List<LectureVideo> allVideos = videoRepository.findAll();
            for (LectureVideo material : allVideos) {

                ResourceFileResponse fileResponse = new ResourceFileResponse();
                fileResponse.setCourseId(material.getCourse().getCourseId());
                fileResponse.setId(material.getId());
                fileResponse.setTitle(material.getTitle());
                fileResponse.setYoutubeVideoId(material.getYoutubeVideoId());
                fileResponse.setType("videos");
                fileResponse.setTimeUploaded(material.getUploadedAt());

                // Add the file to the videos list
                materialsDTO.getVideos().add(fileResponse);

            }
        } else if (type != null && !type.isEmpty()) {
            // Handle other specific types (notes, images, documents)
            for (ResourceDocument material : allMaterials) {
                if (material.getType().equalsIgnoreCase(type)) {
                    ResourceFileResponse fileResponse = new ResourceFileResponse();
                    fileResponse.setCourseId(material.getCourseId());
                    fileResponse.setId(material.getId());
                    fileResponse.setTitle(material.getTitle());
                    fileResponse.setFileUrl(material.getFileUrl()); // Only set fileUrl for non-video types
                    fileResponse.setType(material.getType());
                    fileResponse.setTimeUploaded(material.getTimeCreated());

                    // Add the file to the corresponding list
                    switch (material.getType().toLowerCase()) {
                        case "notes":
                            materialsDTO.getNotes().add(fileResponse);
                            break;
                        case "images":
                            materialsDTO.getImages().add(fileResponse);
                            break;
                        case "documents":
                            materialsDTO.getDocuments().add(fileResponse);
                            break;
                        default:
                            // Handle any unexpected types (if necessary)
                            break;
                    }
                }
            }
        } else {
            // If no type is provided, return all materials (categorizing by type)
            for (ResourceDocument material : allMaterials) {
                ResourceFileResponse fileResponse = new ResourceFileResponse();
                fileResponse.setCourseId(material.getCourseId());
                fileResponse.setId(material.getId());
                fileResponse.setTitle(material.getTitle());
                fileResponse.setFileUrl(material.getFileUrl()); // Set fileUrl for non-video types
                fileResponse.setType(material.getType());
                fileResponse.setTimeUploaded(material.getTimeCreated());

                // Add the file to the correct list based on its type
                switch (material.getType().toLowerCase()) {
                    case "notes":
                        materialsDTO.getNotes().add(fileResponse);
                        break;
                    case "images":
                        materialsDTO.getImages().add(fileResponse);
                        break;
                    case "documents":
                        materialsDTO.getDocuments().add(fileResponse);
                        break;
                    default:
                        // Handle any unexpected types (if necessary)
                        break;
                }
            }
        }

        return materialsDTO; // Return the populated MaterialsDTO
    }



    public ResponseEntity<?> uploadResourceFile(MultipartFile file, String courseId, String title, String type) throws IOException {
        if (courseId.isBlank() || courseId.isEmpty()){
            throw new BadRequestException("Course Id is not provided");
        }
        if (!type.equalsIgnoreCase("notes") && !type.equalsIgnoreCase("images") && !type.equalsIgnoreCase("documents")){
            throw new BadRequestException("Type is not valid. Document, Images, or Notes.");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

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

            ResourceDocument resourceDocument = new ResourceDocument();
            resourceDocument.setFileUrl(url);
            resourceDocument.setCourseId(courseId);
            resourceDocument.setTitle(title);
            resourceDocument.setType(type);

            resourceDocumentRepository.save(resourceDocument);


            return ResponseEntity.ok(url);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file type. Allowed: JPEG, PNG, PDF, DOC, DOCX.");
        }
    }

    public ResponseEntity<?> uploadLessonResourceFile(MultipartFile file, String courseId, String lessonId, String title, String type) throws IOException {
        if (courseId.isBlank() || courseId.isEmpty()){
            throw new BadRequestException("Course Id is not provided");
        }

        if (lessonId.isBlank() || lessonId.isEmpty()){
            throw new BadRequestException("Lesson Id is not provided");
        }
        if (!type.equalsIgnoreCase("notes") && !type.equalsIgnoreCase("images") && !type.equalsIgnoreCase("documents")){
            throw new BadRequestException("Type is not valid. Document, Images, or Notes.");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        if (lesson.getCourse() != course){
            throw new BadRequestException("Lesson is not part of Course");
        }

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

            ResourceDocument resourceDocument = new ResourceDocument();
            resourceDocument.setFileUrl(url);
            resourceDocument.setLessonId(lessonId);
            resourceDocument.setTitle(title);
            resourceDocument.setType(type);

            resourceDocumentRepository.save(resourceDocument);


            return ResponseEntity.ok(url);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file type. Allowed: JPEG, PNG, PDF, DOC, DOCX.");
        }
    }


    public List<ResourceFileResponse> getCourseResourceFiles(String courseId) {
        if (courseId.isBlank() || courseId.isEmpty()){
            throw new BadRequestException("Course Id is not provided");
        }


        return resourceDocumentRepository.findByCourseId(courseId)
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

    public List<ResourceFileResponse> getLessonResourceFiles(String lessonId) {
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


    public String deleteResourceFile(String fileId) {

        if (fileId.isBlank() || fileId.isEmpty()){
            throw new BadRequestException("Resource Document Id is not provided");
        }


        Optional<ResourceDocument> oResourceDocument = resourceDocumentRepository.findById(fileId);
        if (oResourceDocument.isPresent()){
            ResourceDocument resourceDocument = oResourceDocument.get();
            resourceDocumentRepository.delete(resourceDocument);

            return "File Deleted Successfully";
        }else{
            throw new BadRequestException("Document does not exist");
        }

    }

    public CourseDisplayDTO getCourseById(String courseId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        // Fetch course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));



        // Map and return course display info
        return mapCourseToDisplayDTO(course, token);
    }

    public LessonDTO getLessonById(String courseId, String lessonId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        // Fetch course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));


        LessonDTO dto = new LessonDTO();
        dto.setLessonId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setAbout(lesson.getAbout());
        dto.setNotes(lesson.getNotes());
        dto.setTranscript(lesson.getTranscript());
        if (lesson.getVideo() != null){
            dto.setYoutubeVideoId(lesson.getVideo().getYoutubeVideoId());
        }
        dto.setSequenceNumber(lesson.getSequenceNumber());
        dto.setPassingScore(lesson.getPassingScore());
        dto.setCourseId(course.getCourseId());
        dto.setQuizNeeded(lesson.isQuizNeeded());
        dto.setResourceFiles(getLessonResourceFiles(lesson.getId()));


        if (lesson.getQuizId() != null && !lesson.getQuizId().isBlank()) {
            TeacherQuizDTO quiz = webClient.get()
                    .uri("/quizzes/" + lesson.getQuizId() + "/teacher")
                    .headers(headers -> headers.setBearerAuth(token)) // <-- Add Bearer token
                    .retrieve()
                    .bodyToMono(TeacherQuizDTO.class)
                    .block();
            dto.setQuizId(lesson.getQuizId());
            dto.setQuiz(quiz);
        }

        return dto;
    }

    @Transactional
    public LessonDTO addLessonToCourse(String courseId, String adminId, LessonDTO dto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));


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
        }
        if (savedLesson.getQuizId() != null) {
            ldto.setQuizId(savedLesson.getQuizId());
        }

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
        dto.setRatings(courseService.getRatingsForCourse(course.getCourseId())); // If not implemented yet

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

}
