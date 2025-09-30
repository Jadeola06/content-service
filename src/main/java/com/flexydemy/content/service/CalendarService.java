package com.flexydemy.content.service;

import com.flexydemy.content.dto.EventDTO;
import com.flexydemy.content.dto.UserDto;
import com.flexydemy.content.exceptions.BadRequestException;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.Event;
import com.flexydemy.content.repository.EventRepository;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final EventRepository eventRepository;
    private final UserClient userRepository;

    public CalendarService(EventRepository eventRepository, UserClient userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<EventDTO> getCalendarForStudent(String studentId, HttpServletRequest request) {
        String token = Utils.getToken(request);

        if (studentId == null || studentId.isBlank()) {
            throw new BadRequestException("Student ID must be provided");
        }

        UserDto student = userRepository.findById(studentId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", studentId)))
                .block();


        List<Event> events = eventRepository.findByStudentId(studentId);
        

        return events.stream()
                .map(EventDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventDTO> getCalendarForTutor(String tutorId) {
        if (tutorId == null || tutorId.isBlank()) {
            throw new BadRequestException("Tutor ID must be provided");
        }

        List<Event> events = eventRepository.findByTutor_TutorId(tutorId);


        return events.stream()
                .map(EventDTO::new)
                .collect(Collectors.toList());
    }
}
