package com.flexydemy.content.controller;


import com.flexydemy.content.dto.*;
import com.flexydemy.content.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PutMapping("/approve-tutor")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> approveTutor(
            @RequestParam String adminId,
            @RequestParam String tutorId,
            HttpServletRequest request
    ) {
        adminService.approveTutorRequest(adminId, tutorId, request);
        return ResponseEntity.ok("Tutor status updated successfully");
    }

    @PutMapping("/deny-tutor")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> denyTutor(
            @RequestParam String adminId,
            @RequestParam String tutorId,
            HttpServletRequest request
    ) {
        adminService.denyTutorRequest(adminId, tutorId, request);
        return ResponseEntity.ok("Tutor status updated successfully");
    }

    @PutMapping("/flag-tutor")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> flagTutor(
            @RequestParam String adminId,
            @RequestParam String tutorId,
            HttpServletRequest request
    ) {
        adminService.flagTutor(adminId, tutorId, request);
        return ResponseEntity.ok("Tutor status updated successfully");
    }

    @GetMapping("/tutors")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<TutorDTO>> getAllTutors(
            @RequestParam String adminId,
            HttpServletRequest request
    ) {
        List<TutorDTO> tutors = adminService.getAllTutors(adminId, request);
        return ResponseEntity.ok(tutors);
    }

    @GetMapping("/tutors/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<TutorDTO>> getAllPendingTutors(
            @RequestParam String adminId,
            HttpServletRequest request
    ) {
        List<TutorDTO> tutors = adminService.getAllPendingTutors(adminId, request);
        return ResponseEntity.ok(tutors);
    }

    @GetMapping("/students")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<StudentResponse>> getAllStudents(
            @RequestParam String adminId,
            HttpServletRequest request
    ) {
        List<StudentResponse> students = adminService.getAllStudents(adminId, request);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDto> getStudent(
            @RequestParam String adminId,
            @PathVariable String studentId,
            HttpServletRequest request
    ) {
        UserDto user = adminService.getStudentInfo(adminId, studentId, request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/courses")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<CourseDTO>> getAllCourses(
            @RequestParam String adminId,
            HttpServletRequest request
    ) {
        List<CourseDTO> courses = adminService.getAllCourses(adminId, request);
        return ResponseEntity.ok(courses);
    }
    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CourseDisplayDTO> getCourse(
            @PathVariable String courseId,
            HttpServletRequest request
    ) {
        CourseDisplayDTO courses = adminService.getCourseById(courseId, request);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/courses/{courseId}/lessons")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<LessonDTO> addLessonToCourse(
            @PathVariable String courseId,
            @RequestParam String adminId,
            @RequestBody LessonDTO dto
    ) {
        LessonDTO lesson = adminService.addLessonToCourse(courseId, adminId, dto);
        return ResponseEntity.ok(lesson);
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<LessonDTO> getLesson(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            HttpServletRequest request
    ) {
        LessonDTO lesson = adminService.getLessonById(courseId, lessonId, request);
        return ResponseEntity.ok(lesson);
    }




    // ✅ Create FlashCard Set
    @PostMapping("/sets")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<FlashCardSetResponseDTO> createFlashCardSet(
            @RequestBody FlashCardSetRequestDTO dto,
            @RequestParam String adminId,
            HttpServletRequest request
    ) {
        FlashCardSetResponseDTO response = adminService.createFlashCardSet(dto, adminId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    // ✅ Add FlashCard to Set
    @PostMapping("/sets/{setId}/cards")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<FlashCardResponseDTO> addFlashCard(
            @PathVariable String setId,
            @RequestBody FlashCardItemDTO dto,
            @RequestParam String adminId,
            HttpServletRequest request
    ) {
        FlashCardResponseDTO response = adminService.addFlashCard(setId, dto, adminId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ Update FlashCard
    @PutMapping("/sets/cards/{flashCardId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<FlashCardResponseDTO> updateFlashCard(
            @PathVariable String flashCardId,
            @RequestBody FlashCardItemDTO dto,
            @RequestParam String adminId,
            HttpServletRequest request
    ) {
        FlashCardResponseDTO response = adminService.updateFlashCard(flashCardId, dto, adminId, request);
        return ResponseEntity.ok(response);
    }

    // ✅ Delete FlashCard
    @DeleteMapping("/sets/cards/{flashCardId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteFlashCard(
            @PathVariable String flashCardId,
            @RequestParam String adminId,
            HttpServletRequest request
    ) {
        adminService.deleteFlashCard(flashCardId, adminId, request);
        return ResponseEntity.noContent().build();
    }

    // ✅ Delete FlashCard Set
    @DeleteMapping("/sets/{flashCardSetId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteFlashCardSet(
            @PathVariable String flashCardSetId,
            @RequestParam String adminId,
            HttpServletRequest request
    ) {
        adminService.deleteFlashCardSet(flashCardSetId, adminId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/replace/course/{courseId}/tutor/{tutorId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> addTutorToCourse(
            @PathVariable String tutorId,
            @PathVariable String courseId,
            @RequestParam String adminId,
            HttpServletRequest request
    ) {
        String response = adminService.replaceCourseTutor(tutorId, adminId, courseId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assessments/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AssessmentsDTO>> getAllAssessments(
            @RequestParam String adminId,
            @RequestParam(required = false) String type,
            HttpServletRequest request
    ) {
        List<AssessmentsDTO> response = adminService.getAllAssessments(adminId, type, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/courses")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CourseDTO> createCourse(
            @RequestBody CourseDTO dto,
            @RequestParam String adminId
    ) {
        CourseDTO response = adminService.createCourse(dto, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/courses")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> updateCourse(
            @RequestBody CourseDTO dto,
            @RequestParam String adminId
    ) {
        String response = adminService.updateCourse(dto, adminId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/materials/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MaterialsDTO> getAllMaterials(
            @RequestParam String adminId,
            @RequestParam(required = false) String type,
            HttpServletRequest request
    ) {
        MaterialsDTO response =  adminService.getAllMaterials(adminId, type, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/file/course")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> uploadResourceFileToCourse(
            @RequestParam("image") MultipartFile image,
            @RequestParam String courseId,
            @RequestParam String title,
            @RequestParam String type) throws IOException {
        try{
            ResponseEntity<?> imageUrl = adminService.uploadResourceFile(image, courseId, title, type);
            return ResponseEntity.ok(imageUrl);
        }catch (IOException e){
            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());
        }
    }
    @PostMapping("/file/lesson")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> uploadResourceFileToLesson(
            @RequestParam("image") MultipartFile image,
            @RequestParam String courseId,
            @RequestParam String lessonId,
            @RequestParam String title,
            @RequestParam String type) throws IOException {
        try{
            ResponseEntity<?> imageUrl = adminService.uploadLessonResourceFile(image, courseId, lessonId, title, type);
            return ResponseEntity.ok(imageUrl);
        }catch (IOException e){
            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());
        }
    }
    @GetMapping("/file/course")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ResourceFileResponse>> getCourseResourceFiles(@RequestParam String courseId){
        return ResponseEntity.ok(adminService.getCourseResourceFiles(courseId));
    }

    @GetMapping("/file/lesson")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ResourceFileResponse>> getLessonResourceFiles(@RequestParam String lessonId){
        return ResponseEntity.ok(adminService.getLessonResourceFiles(lessonId));
    }

    @DeleteMapping("/file/{fileId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteResourceFile( @PathVariable String fileId){
        return ResponseEntity.ok(adminService.deleteResourceFile(fileId));
    }

}
