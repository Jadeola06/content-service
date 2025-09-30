package com.flexydemy.content.controller;

import com.flexydemy.content.dto.*;
import com.flexydemy.content.service.CourseService;
import com.flexydemy.content.service.YouTubeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.createCourse(courseDTO));
    }

    @PutMapping
    public ResponseEntity<String> updateCourse(@RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.updateCourse(courseDTO));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable String courseId,
            @RequestParam String tutorId
    ) {
        courseService.deleteCourse(courseId, tutorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDisplayDTO> getCourseById(@PathVariable String courseId, @RequestParam String userId, HttpServletRequest request) {
        return ResponseEntity.ok(courseService.getCourseById(courseId, userId, request));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseDTO>> getCoursesBySubjectCategory(@PathVariable String category) {
        return ResponseEntity.ok(courseService.getCoursesBySubjectCategory(category));
    }

    @GetMapping("/category/all")
    public ResponseEntity<List<String>> getAllCourseCategory() {
        return ResponseEntity.ok(courseService.getAllCategoryNames());
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByTutor(@PathVariable String tutorId) {
        return ResponseEntity.ok(courseService.getCoursesByTutor(tutorId));
    }

    @PostMapping("/{courseId}/lessons")
    public ResponseEntity<LessonDTO> addLessonToCourse(
            @PathVariable String courseId,
            @RequestParam String tutorId,
            @RequestBody LessonDTO dto
    ) {
        return ResponseEntity.ok(courseService.addLessonToCourse(courseId, tutorId, dto));
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonDTO> updateLesson(
            @PathVariable String lessonId,
            @RequestParam String tutorId,
            @RequestBody LessonDTO dto
    ) {
        return ResponseEntity.ok(courseService.updateLesson(lessonId, tutorId, dto));
    }

    @PutMapping("/lessons/{lessonId}/quiz/{quizId}")
    public ResponseEntity<TeacherQuizDTO> updateQuiz(
            @PathVariable String lessonId,
            @PathVariable String quizId,
            @RequestBody QuizCreationRequest quiz,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(courseService.updateQuiz(lessonId, quizId, quiz, request ));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonDTO> fetchLessonForStudent(
            @RequestParam String studentId,
            @PathVariable String lessonId,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(courseService.fetchLessonForStudent(studentId, lessonId, request));
    }

    @GetMapping("/lessons/tutor/{lessonId}")
    public ResponseEntity<LessonDTO> fetchLessonForTutor(
            @PathVariable String lessonId,
            @RequestParam String tutorId,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(courseService.fetchLessonForTutor(lessonId, tutorId, request));
    }

    @PostMapping("/lessons/quiz")
    public ResponseEntity<String> addQuizToLesson(@RequestBody QuizCreationRequest quizCreationRequest, HttpServletRequest request) {
        return ResponseEntity.ok(courseService.addQuizToLesson(quizCreationRequest, request));
    }

    @PostMapping("/lessons/csv/quiz")
    public ResponseEntity<QuizResponseDTO> uploadQuizFromCSV(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("tutorId") String tutorId,
                                                    @RequestParam("courseId") String courseId,
                                                    @RequestParam("lessonId") String lessonId,
                                                    @RequestParam("title") String title,
                                                             HttpServletRequest request) {
        return ResponseEntity.ok(courseService.uploadQuizFromCSV(file, tutorId, courseId, lessonId, title, request));
    }

    @PostMapping("/csv/exam")
    public ResponseEntity<QuizResponseDTO> uploadMockExamFromCSV(@RequestParam("file") MultipartFile file,
                                                             @RequestParam("tutorId") String tutorId,
                                                             @RequestParam("title") String title,
                                                                 HttpServletRequest request) {
        return ResponseEntity.ok(courseService.uploadMockExamFromCSV(file, tutorId, title, request));
    }

    @GetMapping("/exams/all")
    private ResponseEntity<List<QuizResponseDTO>> getMockExams(){
        return ResponseEntity.ok(courseService.getAllMockExams());
    }



    @GetMapping("/exams/{mockExamId}")
    public ResponseEntity<StudentQuizDTO> getMockExamForStudent(@PathVariable String mockExamId, HttpServletRequest request) {
        return ResponseEntity.ok(courseService.getMockExamForStudent(mockExamId, request));
    }

    @GetMapping("/exams/tutor/{mockExamId}")
    public ResponseEntity<TeacherQuizDTO> getMockExamForTutor(@PathVariable String mockExamId, @RequestParam String tutorId, HttpServletRequest request) {
        return ResponseEntity.ok(courseService.getMockExamByForTeacher(mockExamId, tutorId, request));
    }

    @PostMapping("/lessons/quiz/submit")
    public ResponseEntity<StudentQuizResultDTO> submitQuizAnswers(@RequestBody QuizSubmitRequest request, HttpServletRequest servletRequest) {
        StudentQuizResultDTO result = courseService.submitQuizAnswers(request, servletRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/exam/submit")
    public ResponseEntity<StudentMockExamResultDTO> submitMockExamAnswers(@RequestBody MockExamSubmitRequest request, HttpServletRequest servletRequest) {
        StudentMockExamResultDTO result = courseService.submitExamAnswers(request, servletRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/rate")
    public ResponseEntity<String> rateCourse(@RequestBody RatingDTO ratingDTO, HttpServletRequest request) {
        return ResponseEntity.ok(courseService.rateCourse(ratingDTO, request));
    }

    @GetMapping("/{courseId}/ratings")
    public ResponseEntity<List<RatingDTO>> getRatingsForCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(courseService.getRatingsForCourse(courseId));
    }

    @GetMapping("/{courseId}/livestreams")
    public ResponseEntity<List<CourseLiveStreamDTO>> getLiveStreamsForCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(courseService.getLiveStreamsForCourse(courseId));
    }



}
