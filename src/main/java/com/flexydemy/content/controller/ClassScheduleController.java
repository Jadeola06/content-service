package com.flexydemy.content.controller;

import com.flexydemy.content.dto.ClassScheduleDTO;
import com.flexydemy.content.service.ClassScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
public class ClassScheduleController {

    private final ClassScheduleService classScheduleService;

    @Autowired
    public ClassScheduleController(ClassScheduleService classScheduleService) {
        this.classScheduleService = classScheduleService;
    }

    @PutMapping
    public ResponseEntity<String> updateClassSchedule(@RequestBody ClassScheduleDTO dto) {
        String result = classScheduleService.updateClassSchedule(dto);
        return ResponseEntity.ok(result);
    }


    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteClassSchedule(@PathVariable String scheduleId) {
        classScheduleService.deleteClassSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ClassScheduleDTO> getClassScheduleById(@PathVariable String scheduleId) {
        ClassScheduleDTO dto = classScheduleService.getClassScheduleById(scheduleId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<ClassScheduleDTO>> getSchedulesByTutor(@PathVariable String tutorId) {
        List<ClassScheduleDTO> schedules = classScheduleService.getClassSchedulesByTutor(tutorId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ClassScheduleDTO>> getSchedulesByStudent(@PathVariable String studentId, HttpServletRequest request) {
        List<ClassScheduleDTO> schedules = classScheduleService.getClassSchedulesByStudent(studentId, request);
        return ResponseEntity.ok(schedules);
    }
}
