package com.flexydemy.content.controller;

import com.flexydemy.content.dto.EventDTO;
import com.flexydemy.content.service.CalendarService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EventDTO>> getStudentCalendar(@PathVariable String studentId, HttpServletRequest request) {
        return ResponseEntity.ok(calendarService.getCalendarForStudent(studentId, request));
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<EventDTO>> getTutorCalendar(@PathVariable String tutorId) {
        return ResponseEntity.ok(calendarService.getCalendarForTutor(tutorId));
    }
}