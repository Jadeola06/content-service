package com.flexydemy.content.controller;

import com.flexydemy.content.dto.StudentCourseProgressDTO;
import com.flexydemy.content.service.StudentCourseProgressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student-progress")
@RequiredArgsConstructor
public class StudentCourseProgressController {

    private final StudentCourseProgressService progressService;

    /**
     * POST /api/v1/student-progress/register
     * Register a student to a course
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerForCourse(
            @RequestParam String userId,
            @RequestParam String courseId,
            HttpServletRequest request
    ) {
        try {
            String responseMessage = progressService.registerForCourse(userId, courseId, request);
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Conflict: " + e.getMessage()); // User already registered
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("Bad Request: " + e.getMessage()); // Invalid input, course/user not found
        }
    }

    /**
     * POST /api/v1/student-progress/complete-lesson
     * Complete a lesson and pass quiz
     */
    @PostMapping("/complete-lesson")
    public ResponseEntity<String> completeLesson(
            @RequestParam String userId,
            @RequestParam String courseId
    ) {
        try {
            progressService.completeLesson(userId, courseId);
            return ResponseEntity.ok("Lesson completed and progress updated.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("Bad Request: " + e.getMessage());
        }
    }

    /**
     * GET /api/v1/student-progress
     * Get progress for a specific course
     */
    @GetMapping
    public ResponseEntity<StudentCourseProgressDTO> getProgress(
            @RequestParam String userId,
            @RequestParam String courseId
    ) {
        return progressService.getProgress(userId, courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(new StudentCourseProgressDTO())); // Not found
    }

    /**
     * GET /api/v1/student-progress/all
     * Get all course progress for a user
     */
    @GetMapping("/all")
    public ResponseEntity<List<StudentCourseProgressDTO>> getAllProgressForUser(
            @RequestParam String userId
    ) {
        List<StudentCourseProgressDTO> progressList = progressService.getAllProgressForUser(userId);
        return progressList.isEmpty()
                ? ResponseEntity.status(404).build() // No progress found for user
                : ResponseEntity.ok(progressList);
    }

    /**
     * GET /api/v1/student-progress/course
     * Get all students' progress for a specific course
     */
    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @GetMapping("/course")
    public ResponseEntity<List<StudentCourseProgressDTO>> getAllProgressForCourse(
            @RequestParam String courseId
    ) {
        List<StudentCourseProgressDTO> progressList = progressService.getAllProgressForCourse(courseId);
        return progressList.isEmpty()
                ? ResponseEntity.status(404).build() // No progress found for course
                : ResponseEntity.ok(progressList);
    }
}
