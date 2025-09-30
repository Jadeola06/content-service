package com.flexydemy.content.service;

import com.flexydemy.content.dto.ClassAttendanceDTO;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.ClassAttendance;
import com.flexydemy.content.dto.UserDto;
import com.flexydemy.content.repository.ClassAttendanceRepository;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassAttendanceService {

    private final ClassAttendanceRepository classAttendanceRepository;
    private final UserClient userRepository;

    /**
     * Mark attendance for a student.
     */
    public ClassAttendanceDTO markAttendance(ClassAttendanceDTO classAttendanceDTO, HttpServletRequest request) {
        String token = Utils.getToken(request);

        UserDto student = userRepository.findById(classAttendanceDTO.getStudentId(), token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student not found")))
                .block();
        ClassAttendance attendance = new ClassAttendance();
        attendance.setStudentId(student.getUserId());
        attendance.setAttended(classAttendanceDTO.isAttended());
        attendance.setAttendanceTime(classAttendanceDTO.isAttended() ? Instant.now() : null);

        ClassAttendance saved = classAttendanceRepository.save(attendance);
        return mapToDTO(saved);
    }

    /**
     * Get all attendance records for a student.
     */
    public List<ClassAttendanceDTO> getAttendanceByStudent(String studentId , HttpServletRequest request) {

        String token = Utils.getToken(request);

        UserDto student = userRepository.findById(studentId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student not found")))
                .block();
        return classAttendanceRepository.findByStudentId(student.getUserId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update a specific attendance record.
     */
    public ClassAttendanceDTO updateAttendance(String attendanceId, boolean attended) {
        ClassAttendance attendance = classAttendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassAttendance", attendanceId.toString()));

        attendance.setAttended(attended);
        attendance.setAttendanceTime(attended ? Instant.now() : null);

        ClassAttendance updated = classAttendanceRepository.save(attendance);
        return mapToDTO(updated);
    }

    /**
     * Delete a specific attendance record.
     */
    public void deleteAttendance(String attendanceId) {
        ClassAttendance attendance = classAttendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassAttendance", attendanceId.toString()));
        classAttendanceRepository.delete(attendance);
    }

    /**
     * Check if a student has already marked attendance today.
     */
    public boolean hasMarkedAttendanceToday(String studentId, HttpServletRequest request) {

        String token = Utils.getToken(request);
        UserDto student = userRepository.findById(studentId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student not found")))
                .block();

        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        assert student != null;
        return classAttendanceRepository.existsByStudentIdAndAttendanceTimeAfter(student.getUserId(), startOfDay);
    }

    /**
     * Utility method to map ClassAttendance to DTO.
     */
    private ClassAttendanceDTO mapToDTO(ClassAttendance attendance) {
        return new ClassAttendanceDTO(
                attendance.getStudentId(),
                attendance.isAttended(),
                attendance.getAttendanceTime()
        );
    }
}
