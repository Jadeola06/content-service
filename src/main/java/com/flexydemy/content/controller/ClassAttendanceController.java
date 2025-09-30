package com.flexydemy.content.controller;

import com.flexydemy.content.dto.ClassAttendanceDTO;
import com.flexydemy.content.service.ClassAttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class ClassAttendanceController {

    private final ClassAttendanceService classAttendanceService;

    @PostMapping("/mark")
    public ClassAttendanceDTO markAttendance(@RequestBody ClassAttendanceDTO dto, HttpServletRequest request) {
        return classAttendanceService.markAttendance(dto, request);
    }

    @GetMapping("/student/{studentId}")
    public List<ClassAttendanceDTO> getAttendanceByStudent(@PathVariable String studentId, HttpServletRequest request) {
        return classAttendanceService.getAttendanceByStudent(studentId, request);
    }

    @PutMapping("/update/{attendanceId}")
    public ClassAttendanceDTO updateAttendance(@PathVariable String attendanceId,
                                               @RequestParam boolean attended) {
        return classAttendanceService.updateAttendance(attendanceId, attended);
    }

    @DeleteMapping("/delete/{attendanceId}")
    public void deleteAttendance(@PathVariable String attendanceId) {
        classAttendanceService.deleteAttendance(attendanceId);
    }

    @GetMapping("/check-today/{studentId}")
    public boolean hasMarkedAttendanceToday(@PathVariable String studentId, HttpServletRequest request) {
        return classAttendanceService.hasMarkedAttendanceToday(studentId, request);
    }
}
