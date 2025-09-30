package com.flexydemy.content.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.flexydemy.content.dto.*;
import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.exceptions.BadRequestException;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.*;
import com.flexydemy.content.repository.*;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import com.cloudinary.utils.ObjectUtils;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private StudentCourseProgressService studentCourseProgressService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseProgressRepository progressRepository;

    @Autowired
    private StudentLessonProgressRepository lessonProgressRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserClient userRepository;

    @Autowired
    private AchievementRepository achievementRepository;



    public List<UserCourseResponse> getAllCoursesEnrolledIn(String userId) {
        if (userId.isBlank() || userId.isEmpty()){
            throw new BadRequestException("User Id is not provided");
        }
        List<StudentCourseProgressDTO> list = studentCourseProgressService.getAllProgressForUser(userId);

        return list.stream().map(progressDTO -> {
            Optional<Course> optionalCourse = courseRepository.findById(progressDTO.getCourseId());

            if (optionalCourse.isEmpty()) {
                return null; // or throw an exception, or filter out later
            }

            Course course = optionalCourse.get();

            UserCourseResponse response = new UserCourseResponse();
            response.setCourseId(course.getCourseId());
            response.setCourseTitle(course.getCourseTitle());
            response.setDescription(course.getDescription());
            response.setStudentId(progressDTO.getUserId());
            response.setTutorId(course.getTutor().getTutorId());
            response.setSubjectCategory(course.getSubject());
            response.setGradeLevel(course.getGradeLevel());
            response.setPublished(course.isPublished());
            response.setDuration(course.getDuration());
            response.setRatingsCount(course.getRatingsCount());
            response.setAverageRating(course.getAverageRating());
            response.setProgressPercentage(progressDTO.getProgressPercentage());


            return response;
        }).filter(Objects::nonNull).toList();
    }

    public List<UserCourseResponse> getUserCoursesBySubjectCategory(String userId, String categoryName) {
        if (userId.isBlank() || userId.isEmpty()){
            throw new BadRequestException("User Id is not provided");
        }
        if (categoryName.isBlank() || categoryName.isEmpty()){
            throw new BadRequestException("Course Category is not provided");
        }
        Class_Categories category = Arrays.stream(Class_Categories.values())
                .filter(c -> c.name().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid subject category: " + categoryName));


        // Get all courses the user is enrolled in
        List<StudentCourseProgressDTO> progressList = studentCourseProgressService.getAllProgressForUser(userId);

        return progressList.stream()
                .map(progress -> courseRepository.findById(progress.getCourseId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(course -> course.getSubject() == category)
                .map(course -> {
                    UserCourseResponse response = new UserCourseResponse();
                    response.setCourseId(course.getCourseId());
                    response.setCourseTitle(course.getCourseTitle());
                    response.setDescription(course.getDescription());
                    response.setTutorId(course.getTutor().getTutorId());
                    response.setSubjectCategory(course.getSubject());
                    response.setGradeLevel(course.getGradeLevel());
                    response.setPublished(course.isPublished());
                    response.setDuration(course.getDuration());
                    response.setRatingsCount(course.getRatingsCount());
                    response.setAverageRating(course.getAverageRating());

                    // Match the progress info
                    StudentCourseProgressDTO progressDTO = progressList.stream()
                            .filter(p -> p.getCourseId().equals(course.getCourseId()))
                            .findFirst()
                            .orElse(null);

                    if (progressDTO != null) {
                        response.setStudentId(progressDTO.getUserId());
                        response.setProgressPercentage(progressDTO.getProgressPercentage());
                    }




                    return response;
                })
                .toList();
    }

    public StudentDashboardDTO getUserDashboard(String userId) {
        if (userId.isBlank() || userId.isEmpty()){
            throw new BadRequestException("User Id is not provided");
        }
        List<StudentCourseProgress> progressList = progressRepository.findByUserId(userId);

        DashboardProgressDTO notStarted = new DashboardProgressDTO();
        DashboardProgressDTO classesInProgress = new DashboardProgressDTO();
        DashboardProgressDTO classesFinished = new DashboardProgressDTO();
        DashboardProgressDTO totalClasses = new DashboardProgressDTO();

        List<String> classesNotStarted = new ArrayList<>();
        List<String> inProgressClasses = new ArrayList<>();
        List<String> finishedClasses = new ArrayList<>();
        Set<String> allClasses = new HashSet<>();

        for (StudentCourseProgress progress : progressList) {
            Course course = progress.getCourse();
            if (course == null || course.getCourseTitle() == null) continue;

            String courseTitle = course.getCourseTitle();
            allClasses.add(courseTitle);

            if (progress.isCompleted()) {
                finishedClasses.add(courseTitle);
            } else if (progress.getProgressPercentage() > 0 && progress.getProgressPercentage() < 90) {
                inProgressClasses.add(courseTitle);
            } else if (progress.getCurrentLessonSequence() == 1) {
                classesNotStarted.add(courseTitle);
            }
        }

        notStarted.setClassCount(classesNotStarted.size());
        notStarted.setClasses(classesNotStarted);

        classesInProgress.setClassCount(inProgressClasses.size());
        classesInProgress.setClasses(inProgressClasses);

        classesFinished.setClassCount(finishedClasses.size());
        classesFinished.setClasses(finishedClasses);

        totalClasses.setClassCount(allClasses.size());
        totalClasses.setClasses(new ArrayList<>(allClasses));

        // ==== Learning Time Calculation ====
        List<StudentLessonProgress> lessonProgresses = lessonProgressRepository.findByCourseProgress_UserId(userId);
        Map<String, LearningTimeByMonthDTO> monthlyMap = new HashMap<>();

        for (StudentLessonProgress lessonProgress : lessonProgresses) {
            if (!lessonProgress.isCompleted() || lessonProgress.getCompletedAt() == null) continue;

            Lesson lesson = lessonProgress.getLesson();
            if (lesson == null || lesson.getDuration() == null) continue;

            int durationInMinutes;
            try {
                durationInMinutes = Integer.parseInt(lesson.getDuration());
            } catch (NumberFormatException e) {
                continue;
            }

            double durationInHours = durationInMinutes / 60.0;
            LocalDateTime completedAt = lessonProgress.getCompletedAt();
            String displayMonth = completedAt.getMonth().name().substring(0, 1) + completedAt.getMonth().name().substring(1).toLowerCase();

            LearningTimeByMonthDTO dto = monthlyMap.getOrDefault(displayMonth, new LearningTimeByMonthDTO(displayMonth, completedAt.getMonthValue(), 0, 0, 0));
            int hour = completedAt.getHour();

            if (hour >= 5 && hour < 12) {
                dto.setMorningHours(dto.getMorningHours() + durationInHours);
            } else if (hour >= 12 && hour < 18) {
                dto.setAfternoonHours(dto.getAfternoonHours() + durationInHours);
            } else {
                dto.setEveningHours(dto.getEveningHours() + durationInHours);
            }

            monthlyMap.put(displayMonth, dto);
        }

        List<LearningTimeByMonthDTO> learningTimeDTOs = new ArrayList<>(monthlyMap.values());
        learningTimeDTOs.sort(Comparator.comparing(dto -> Month.valueOf(dto.getMonth().toUpperCase()).getValue()));

        // ==== Upcoming Sessions ====
        List<UpcomingSessions> upcomingSessions = sessionRepository
                .findUpcomingSessionsByUserId(userId, LocalDateTime.now())
                .stream()
                .map(session -> {
                    UpcomingSessions dto = new UpcomingSessions();
                    dto.setSessionId(session.getId());
                    dto.setSessionName(session.getName());
                    //dto.setTutorName(session.getTutor().getFullName());
                    dto.setTime(session.getScheduledTime());
                    return dto;
                })
                .collect(Collectors.toList());

        // ==== Continue Learning ====
        List<ContinueLearning> continueLearnings = progressList.stream()
                .filter(p -> !p.isCompleted())
                .sorted(Comparator.comparingDouble(StudentCourseProgress::getProgressPercentage).reversed())
                .map(progress -> {
                    ContinueLearning dto = new ContinueLearning();
                    dto.setCourseId(progress.getCourse().getCourseId());
                    dto.setCourseName(progress.getCourse().getCourseTitle());
                    dto.setPercentage(progress.getProgressPercentage());

                    Optional<Lesson> nextLesson = lessonRepository.findByCourseAndSequenceNumber(progress.getCourse(), progress.getCurrentLessonSequence());
                    nextLesson.ifPresent(lesson -> dto.setNextLesson(lesson.getTitle()));

                    return dto;
                })
                .collect(Collectors.toList());

        // ==== Recent Achievements ====
        List<AchievementResponse> recentAchievements = achievementRepository
                .findTop2ByUserIdOrderByAchievedAtDesc(userId)
                .stream()
                .map(a -> {
                    AchievementResponse response = new AchievementResponse();
                    response.setAchievement(a.getAchievement().getName());
                    response.setDescription(a.getAchievement().getDescription());
                    response.setDateAccomplished(a.getAchievedAt());
                    return response;
                })
                .collect(Collectors.toList());

        // ==== Learning Analytics ====
        LearningAnalytics analytics = new LearningAnalytics();

        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        int studyTimeThisWeek = lessonProgressRepository
                .findByCourseProgress_UserIdAndCompletedAtAfter(userId, weekStart)
                .stream()
                .mapToInt(lp -> {
                    try {
                        return Integer.parseInt(lp.getLesson().getDuration());
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();
        analytics.setStudyTimeThisWeekInMinutes(studyTimeThisWeek);

        double quizAccuracy = findAccuracyByUserId(userId); // Implement accordingly
        analytics.setQuizAccuracy(quizAccuracy);

        Set<LocalDate> activeDays = lessonProgressRepository.findByCourseProgress_UserId(userId).stream()
                .filter(StudentLessonProgress::isCompleted)
                .map(lp -> lp.getCompletedAt().toLocalDate())
                .collect(Collectors.toSet());
        analytics.setDaysActive(activeDays.size());

        analytics.setRankInClass(3); // Replace with real ranking logic

        // ==== Final Assembly ====
        StudentDashboardDTO dashboardDTO = new StudentDashboardDTO();
        dashboardDTO.setStudentId(userId);
        dashboardDTO.setNotStarted(notStarted);
        dashboardDTO.setClassesInProgress(classesInProgress);
        dashboardDTO.setClassesCompleted(classesFinished);
        dashboardDTO.setTotalClasses(totalClasses);
        dashboardDTO.setLearningTimesByMonth(learningTimeDTOs);
        dashboardDTO.setUpcomingSessions(upcomingSessions);
        dashboardDTO.setContinueLearnings(continueLearnings);
        dashboardDTO.setRecentAchievements(recentAchievements);
        dashboardDTO.setLearningAnalytics(analytics);

        return dashboardDTO;
    }
    


    public List<UserCourseResponse> getAllCoursesByClassForUser(String userId, HttpServletRequest request) {
        if (userId.isBlank() || userId.isEmpty()){
            throw new BadRequestException("User Id is not provided");
        }
        String token = Utils.getToken(request);

        if (userId == null) {
            throw new BadRequestException("User Id is Required.");
        }

        UserDto student = userRepository.findById(userId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student not found")))
                .block();



        return courseRepository.findBySubject(student.getClassCategory()).stream().map(course -> {
                    UserCourseResponse response = new UserCourseResponse();
                    response.setCourseId(course.getCourseId());
                    response.setCourseTitle(course.getCourseTitle());
                    response.setDescription(course.getDescription());
                    response.setTutorId(course.getTutor().getTutorId());
                    response.setSubjectCategory(course.getSubject());
                    response.setGradeLevel(course.getGradeLevel());
                    response.setPublished(course.isPublished());
                    response.setDuration(course.getDuration());
                    response.setRatingsCount(course.getRatingsCount());
                    response.setAverageRating(course.getAverageRating());



                    return response;
                })
                .toList();
    }

    public double findAccuracyByUserId(String userId) {
        List<StudentLessonProgress> progresses = lessonProgressRepository.findByCourseProgress_UserId(userId);

        long quizzesAttempted = progresses.stream()
                .filter(p -> p.getQuizScore() > 0)
                .count();

        if (quizzesAttempted == 0) return 0.0;

        long quizzesPassed = progresses.stream()
                .filter(p -> p.getQuizScore() > 0 && p.isPassedQuiz())
                .count();

        return ((double) quizzesPassed / quizzesAttempted) * 100.0;
    }

}
