package com.flexydemy.content.service;

import com.flexydemy.content.dto.ClassScheduleDTO;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.ClassSchedule;
import com.flexydemy.content.model.Tutor;
import com.flexydemy.content.dto.UserDto;
import com.flexydemy.content.repository.ClassScheduleRepository;
import com.flexydemy.content.repository.CourseRepository;
import com.flexydemy.content.repository.TutorRepository;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassScheduleService {

    private final ClassScheduleRepository classScheduleRepository;
    private final TutorRepository tutorRepository;
    private final CourseRepository courseRepository;
    private final UserClient userRepository;

    @Autowired
    public ClassScheduleService(ClassScheduleRepository classScheduleRepository, TutorRepository tutorRepository, CourseRepository courseRepository, UserClient userRepository) {
        this.classScheduleRepository = classScheduleRepository;
        this.tutorRepository = tutorRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public String updateClassSchedule(ClassScheduleDTO dto) {
        ClassSchedule schedule = classScheduleRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", dto.getId()));

        schedule.setDescription(dto.getDescription());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setMeetingLink(dto.getMeetingLink());
        schedule.setRecordedVideoUrl(dto.getRecordedVideoUrl());
        schedule.setMaximumStudents(dto.getMaximumStudents());

        classScheduleRepository.save(schedule);
        return "Class Schedule Updated";
    }

    public void deleteClassSchedule(String scheduleId) {
        if (!classScheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException("Schedule", scheduleId);
        }
        classScheduleRepository.deleteById(scheduleId);
    }

    public ClassScheduleDTO getClassScheduleById(String scheduleId) {
        ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", scheduleId));
        return toDTO(schedule);
    }

    public List<ClassScheduleDTO> getClassSchedulesByTutor(String tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", tutorId));
        return classScheduleRepository.findByTutor(tutor)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ClassScheduleDTO> getClassSchedulesByStudent(String studentId, HttpServletRequest request) {

        String token = Utils.getToken(request);

        UserDto student = userRepository.findById(studentId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student",studentId)))
                .block();

        return classScheduleRepository.findByStudentIdsContaining(studentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    private ClassScheduleDTO toDTO(ClassSchedule schedule) {
        ClassScheduleDTO dto = new ClassScheduleDTO();
        dto.setId(schedule.getId());
        dto.setTutorId(schedule.getTutor().getTutorId());
        dto.setDescription(schedule.getDescription());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setMeetingLink(schedule.getMeetingLink());
        dto.setRecordedVideoUrl(schedule.getRecordedVideoUrl());
        dto.setMaximumStudents(schedule.getMaximumStudents());
        return dto;
    }
}
