package com.flexydemy.content.controller;

import com.flexydemy.content.dto.ResourceFileResponse;
import com.flexydemy.content.dto.StudentDashboardDTO;
import com.flexydemy.content.dto.UserCourseResponse;
import com.flexydemy.content.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<StudentDashboardDTO> getStudentDashboard(@PathVariable String userId){
        return ResponseEntity.ok(userService.getUserDashboard(userId));
    }

    @GetMapping("/courses/{userId}")
    public ResponseEntity<List<UserCourseResponse>> getAllClassCoursesForUser(@PathVariable String userId, HttpServletRequest request){
        return ResponseEntity.ok(userService.getAllCoursesByClassForUser(userId, request));
    }

    @GetMapping("/enrolled/courses/{userId}")
    public ResponseEntity<List<UserCourseResponse>> getAllCoursesEnrolledIn(@PathVariable String userId){
        return ResponseEntity.ok(userService.getAllCoursesEnrolledIn(userId));
    }

    @GetMapping("/courses/{userId}/subject/{subject}")
    public ResponseEntity<List<UserCourseResponse>> getUserCoursesBySubjectCategory(@PathVariable String userId, @PathVariable String subject){
        return ResponseEntity.ok(userService.getUserCoursesBySubjectCategory(userId,subject));
    }




}
