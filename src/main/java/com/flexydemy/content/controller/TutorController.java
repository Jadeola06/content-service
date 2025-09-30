package com.flexydemy.content.controller;

import com.flexydemy.content.dto.*;
import com.flexydemy.content.model.TutorSessionStudentDTO;
import com.flexydemy.content.service.TutorService;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tutors")
public class TutorController {

    private final TutorService tutorService;

    @Autowired
    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @PostMapping("/create")
    public ResponseEntity<TutorDTO> registerTutor(@RequestBody RegisterTutorDTO dto) {
        TutorDTO tutorDTO = tutorService.registerTutor(dto);
        return ResponseEntity.ok(tutorDTO);
    }

    @PutMapping("/{tutorId}")
    public ResponseEntity<TutorDTO> updateTutor(@PathVariable String tutorId, @RequestBody UpdateTutorDTO dto, HttpServletRequest request) {
        TutorDTO updatedTutor = tutorService.updateTutor(dto, request);
        return ResponseEntity.ok(updatedTutor);
    }

    @PutMapping("/resume/{tutorId}")
    public ResponseEntity<TutorDTO> updateTutorResume(@PathVariable String tutorId, @RequestParam MultipartFile file, @RequestBody UpdateTutorDTO dto, HttpServletRequest request) {
        TutorDTO updatedTutor = tutorService.updateTutorResume(tutorId, file, request);
        return ResponseEntity.ok(updatedTutor);
    }

    @DeleteMapping("/{tutorId}")
    public ResponseEntity<Void> deleteTutor(@PathVariable String tutorId) {
        tutorService.deleteTutor(tutorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tutorId}")
    public ResponseEntity<TutorDTO> getTutorById(@PathVariable String tutorId, HttpServletRequest request) {
        TutorDTO tutor = tutorService.getTutorById(tutorId, request);
        return ResponseEntity.ok(tutor);
    }

    @GetMapping
    public ResponseEntity<List<TutorDTO>> getAllTutors(HttpServletRequest request) {
        List<TutorDTO> tutors = tutorService.getAllTutors(request);
        return ResponseEntity.ok(tutors);
    }

    @PostMapping("/rate")
    public ResponseEntity<String> rateTutor(@RequestBody RatingDTO dto, HttpServletRequest request) {
        String rating = tutorService.rateTutor(dto, request);
        return ResponseEntity.ok(rating);
    }

    @GetMapping("/{tutorId}/ratings")
    public ResponseEntity<List<RatingDTO>> getTutorRatings(@PathVariable String tutorId) {
        List<RatingDTO> ratings = tutorService.getRatingsForTutor(tutorId);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/{tutorId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable String tutorId) {
        double average = tutorService.calculateAverageRating(tutorId);
        return ResponseEntity.ok(average);
    }

    @GetMapping("/dashboard/{tutorId}")
    public ResponseEntity<TutorDashboardDTO> getTutorDashboard(
            @PathVariable String tutorId,
            HttpServletRequest request) {

        TutorDashboardDTO dashboard = tutorService.getTutorDashboard(tutorId, request);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/search")
    public List<TutorDTO> searchTutorsBySubject(@RequestParam String category,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                HttpServletRequest request) {
        return tutorService.searchTutorsBySubject(category, page, size, request);
    }

    @PostMapping("/resume/{tutorId}")
    public ResponseEntity<?> uploadResume(@RequestParam("image") MultipartFile image, @PathVariable String tutorId) throws IOException {
        try{
            ResponseEntity<?> imageUrl = tutorService.uploadResume(image, tutorId);
            return ResponseEntity.ok(imageUrl);
        }catch (IOException e){
            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());
        }
    }
    @GetMapping("/resume/{tutorId}")
    public ResponseEntity<String> getTutorResume(
            @PathVariable String tutorId) {

        String resumeUrl = tutorService.getTutorResume(tutorId);
        return ResponseEntity.ok(resumeUrl);
    }

    // Get all students for a tutor
    @GetMapping("/{tutorId}/students")
    public ResponseEntity<List<TutorStudentSummaryDTO>> getAllTutorStudents(
            @PathVariable String tutorId,
            HttpServletRequest request) {

        List<TutorStudentSummaryDTO> students = tutorService.getAllTutorStudents(tutorId, request);
        return ResponseEntity.ok(students);
    }

    // Search students by keyword (name or course title)
    @GetMapping("/{tutorId}/students/search")
    public ResponseEntity<List<TutorStudentSummaryDTO>> searchTutorStudents(
            @PathVariable String tutorId,
            @RequestParam("keyword") String keyword,
            HttpServletRequest request) {

        List<TutorStudentSummaryDTO> results = tutorService.searchTutorStudents(tutorId, keyword, request);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{tutorId}/sessions/all")
    public ResponseEntity<Page<TutorSessionStudentDTO>> getAllTutorSessions(
            @PathVariable String tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String token = Utils.getToken(request);
        Page<TutorSessionStudentDTO> result = tutorService.getAllTutorSessions(tutorId, token, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{tutorId}/sessions/upcoming")
    public ResponseEntity<Page<TutorSessionStudentDTO>> getUpcomingTutorSessions(
            @PathVariable String tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String token = Utils.getToken(request);
        Page<TutorSessionStudentDTO> result = tutorService.getUpcomingSessions(tutorId, token, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{tutorId}/sessions/completed")
    public ResponseEntity<Page<TutorSessionStudentDTO>> getCompletedTutorSessions(
            @PathVariable String tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String token = Utils.getToken(request);
        Page<TutorSessionStudentDTO> result = tutorService.getCompletedSessions(tutorId, token, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{tutorId}/sessions/missed")
    public ResponseEntity<Page<TutorSessionStudentDTO>> getMissedTutorSessions(
            @PathVariable String tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String token = Utils.getToken(request);
        Page<TutorSessionStudentDTO> result = tutorService.getMissedSessions(tutorId, token, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{tutorId}/courses/analytics")
    public ResponseEntity<List<TutorCourseAnalyticsDTO>> getTutorCoursesAnalytics(@PathVariable String tutorId) {
        List<TutorCourseAnalyticsDTO> result = tutorService.getTutorCourseAnalytics(tutorId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{tutorId}/courses/all")
    public ResponseEntity<List<TutorCourseAnalyticsDTO>> getTutorCourses(@PathVariable String tutorId) {
        List<TutorCourseAnalyticsDTO> result = tutorService.getTutorCourseAnalytics(tutorId);
        return ResponseEntity.ok(result);
    }
}
