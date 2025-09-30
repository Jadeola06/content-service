package com.flexydemy.content.service;

import com.flexydemy.content.dto.StudentCourseProgressDTO;
import com.flexydemy.content.enums.NotificationTypes;
import com.flexydemy.content.exceptions.BadRequestException;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.*;
import com.flexydemy.content.dto.UserDto;
import com.flexydemy.content.repository.*;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentCourseProgressService {

    private final StudentCourseProgressRepository progressRepository;
    private final StudentLessonProgressRepository lessonProgressRepository;
    private final UserClient userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final NotificationService notificationService;

    /**
     * Register a user for a course and initialize their progress
     */
    @Transactional
    public String registerForCourse(String userId, String courseId, HttpServletRequest request) {
        String token  = Utils.getToken(request);
        UserDto user = userRepository.findById(userId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student not found")))
                .block();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if the user is already registered for the course
        if (progressRepository.findByUserIdAndCourse_CourseId(user.getUserId(), course.getCourseId()).isPresent()) {
            throw new IllegalStateException("User already registered for course");
        }

        // Create course progress
        StudentCourseProgress progress = new StudentCourseProgress();
        progress.setUserId(user.getUserId());
        progress.setUsername(user.getUsername());
        progress.setCourse(course);
        progress.setCurrentLessonSequence(1);
        progress = progressRepository.save(progress);

        // Initialize first lesson progress
        Lesson firstLesson = lessonRepository.findByCourseAndSequenceNumber(course, 1)
                .orElseThrow(() -> new RuntimeException("First lesson not found"));

        StudentLessonProgress lessonProgress = new StudentLessonProgress();
        lessonProgress.setCourseProgress(progress);
        lessonProgress.setLesson(firstLesson);
        lessonProgress.setCompleted(false);
        lessonProgress.setPassedQuiz(false);
        lessonProgressRepository.save(lessonProgress);

        // Enroll in the first available class schedule for the course
        enrollInClassSchedule(user, course);

        // Add the course to the user's course list
        user.getCourseList().add(course);

        return "Registered for course: " + course.getCourseTitle();
    }

    /**
     * Enroll the user in the first available class schedule for the course
     */
    private void enrollInClassSchedule(UserDto user, Course course) {
        classScheduleRepository.findByCourse(course).stream()
                .findFirst()
                .ifPresent(schedule -> {
                    schedule.getStudentIds().add(user.getUserId());
                    classScheduleRepository.save(schedule);
                });
    }

    /**
     * Mark a lesson as completed and passed, then unlock next lesson if applicable
     */
    @Transactional
    public void completeLesson(String userId, String courseId) {
        // Get the student's course progress
        StudentCourseProgress progress = progressRepository
                .findByUserIdAndCourse_CourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found"));

        // Find the lesson for the given sequence number
        Lesson lesson = lessonRepository.findByCourseAndSequenceNumber(progress.getCourse(), progress.getCurrentLessonSequence())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Find the student's progress for this lesson
        StudentLessonProgress lessonProgress = lessonProgressRepository
                .findByCourseProgressAndLesson(progress, lesson)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson progress not found"));

        double quizScore = lessonProgress.getQuizScore();

        // Validate quiz score if quiz is needed
        boolean passed = validateQuiz(lesson, quizScore);

        // Update the lesson progress
        lessonProgress.setCompleted(true);
        lessonProgress.setCompletedAt(java.time.LocalDateTime.now());
        lessonProgressRepository.save(lessonProgress);

        long totalLessons = lessonRepository.countByCourse_CourseId(courseId);

        // If passed the quiz, unlock the next lesson
        if (lesson.getSequenceNumber() == totalLessons && passed) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        } else if (passed) {
            unlockNextLesson(progress, lesson, progress.getCurrentLessonSequence());
        }

        // Update the overall progress percentage for the course
        progress.setProgressPercentage(calculateProgressPercentage(progress));

        //Send Notifications
        if (calculateProgressPercentage(progress) >= 50 && !progress.isFiftyPercentNotificationSent()){
            notificationService.sendNotification(userId,
                    NotificationTypes.COURSE_PROGRESS,
                    "Course Progress",
                    "✅Congrats! You've completed 50% of your " + course.getCourseTitle() + " lessons.",
                    courseId,
                    null);
            progress.setFiftyPercentNotificationSent(true);
        } else if (calculateProgressPercentage(progress) >= 80  && !progress.isEightyPercentNotificationSent()){
            notificationService.sendNotification(userId,
                    NotificationTypes.COURSE_PROGRESS,
                    "Course Progress",
                    "✅Congrats! You've completed 80% of your " + course.getCourseTitle() + " lessons.",
                    courseId,
                    null);
            progress.setEightyPercentNotificationSent(true);
        }
        progressRepository.save(progress);
    }

    /**
     * Validate the quiz score against the passing score for the lesson
     */
    private boolean validateQuiz(Lesson lesson, double quizScore) {
        if (lesson.isQuizNeeded()) {
            if (quizScore < 0 || quizScore > 100) {
                throw new BadRequestException("Quiz score must be between 0 and 100");
            }
            return quizScore >= lesson.getPassingScore();
        }
        return true;
    }

    /**
     * Unlock the next lesson after completing the current one
     */
    private void unlockNextLesson(StudentCourseProgress progress, Lesson currentLesson, int sequenceNumber) {
        int nextSeq = sequenceNumber + 1;
        Optional<Lesson> nextLesson = lessonRepository.findByCourseAndSequenceNumber(progress.getCourse(), nextSeq);
        nextLesson.ifPresent(lesson -> {
            progress.setCurrentLessonSequence(nextSeq);

            StudentLessonProgress nextLessonProgress = new StudentLessonProgress();
            nextLessonProgress.setCourseProgress(progress);
            nextLessonProgress.setLesson(lesson);
            nextLessonProgress.setCompleted(false);
            nextLessonProgress.setPassedQuiz(false);

            lessonProgressRepository.save(nextLessonProgress);
        });
    }

    /**
     * Calculate the overall progress percentage for the course
     */
    private double calculateProgressPercentage(StudentCourseProgress progress) {
        int totalLessons = lessonRepository.countByCourse(progress.getCourse());
        int completedLessons = lessonProgressRepository.countByCourseProgressAndCompletedTrue(progress);
        return (double) completedLessons / totalLessons * 100;
    }

    /**
     * Get all progress for a specific course
     */
    public List<StudentCourseProgressDTO> getAllProgressForCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<StudentCourseProgress> progressList = progressRepository.findAllByCourse(course);

        return progressList.stream().map(progress -> convertToDTO(progress)).toList();
    }

    /**
     * Get progress for a specific user in a course
     */
    public Optional<StudentCourseProgressDTO> getProgress(String userId, String courseId) {
        return progressRepository.findByUserIdAndCourse_CourseId(userId, courseId)
                .map(progress -> convertToDTO(progress));
    }

    /**
     * Get all progress for a user across all courses
     */
    public List<StudentCourseProgressDTO> getAllProgressForUser(String userId) {
        List<StudentCourseProgress> progressList = progressRepository.findAllByUserId(userId);

        return progressList.stream().map(progress -> convertToDTO(progress)).toList();
    }

    /**
     * Convert StudentCourseProgress to StudentCourseProgressDTO
     */
    private StudentCourseProgressDTO convertToDTO(StudentCourseProgress progress) {
        String userId = progress.getUserId();
        Course course = progress.getCourse();
        return new StudentCourseProgressDTO(
                userId,
                progress.getUsername(),
                course.getCourseId(),
                course.getCourseTitle(),
                progress.getCurrentLessonSequence(),
                progress.getProgressPercentage()
        );
    }

}
