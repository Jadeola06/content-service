package com.flexydemy.content.controller;

import com.flexydemy.content.dto.*;
import com.flexydemy.content.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;


    @PostMapping("/tutor/create")
    public ResponseEntity<SessionResponseDTO> createSessionByTutor(@RequestBody TutorSessionCreateRequestDTO dto, HttpServletRequest request) {
        return ResponseEntity.ok(sessionService.createSessionByTutor(dto, request));
    }
    //Book Session From Display
    @PostMapping("/book")
    public ResponseEntity<SessionResponseDTO> bookDisplaySession(@RequestBody BookSessionRequestDTO dto, HttpServletRequest request) {
        return ResponseEntity.ok(sessionService.bookMadeSession(dto, request));
    }
    //Approve Session From Display
    @PostMapping("/approve")
    public ResponseEntity<SessionResponseDTO> approveBookedSession(@RequestBody SessionApprovalRequest approvalRequest) {
        return ResponseEntity.ok(sessionService.approveBookedSession(approvalRequest));
    }
    /**
     * Student requests a custom 1-on-1 session
     */
    @PostMapping("/request-custom")
    public ResponseEntity<SessionResponseDTO> requestCustomSession(
            @RequestBody CustomSessionRequestDTO requestDto,
            HttpServletRequest request
    ) {
        SessionResponseDTO response = sessionService.requestCustomSession(requestDto, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Tutor approves a pending custom session
     */
    @PostMapping("/approve-custom")
    public ResponseEntity<SessionResponseDTO> approveCustomSession(
            @RequestBody SessionApprovalRequest approvalRequest
    ) {
        SessionResponseDTO response = sessionService.approveCustomSession(approvalRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/reject")
    public ResponseEntity<String> rejectSession(
            @PathVariable String sessionId,
            @RequestParam String tutorId) {
        sessionService.rejectSession(sessionId, tutorId);
        return ResponseEntity.ok("Session rejected successfully.");
    }

    @PostMapping("/{sessionId}/start-session")
    public ResponseEntity<SessionResponseDTO> startSession(
            @PathVariable String sessionId,
            @RequestParam String tutorId,
            HttpServletRequest request) {
        SessionResponseDTO response = sessionService.startSessionMeeting(sessionId, tutorId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/rejoin-session")
    public ResponseEntity<SessionResponseDTO> tutorRejoinSession(
            @PathVariable String sessionId,
            @RequestParam String tutorId,
            HttpServletRequest request) {
        SessionResponseDTO response = sessionService.tutorRejoinMeeting(sessionId, tutorId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/join")
    public ResponseEntity<SessionResponseDTO> joinLiveSession(
            @PathVariable String sessionId,
            @RequestParam String studentId,
            HttpServletRequest request) {

        return ResponseEntity.ok(sessionService.joinMeeting(sessionId, studentId, request));
    }


    @GetMapping("/tutors/display-sessions/{tutorId}")
    public ResponseEntity<List<TutorDisplaySessionDTO>> getAvailableSessions(@PathVariable String tutorId) {
        return ResponseEntity.ok(sessionService.getAvailableSessions(tutorId));
    }

    @GetMapping("/tutors/sessions/{tutorId}")
    public ResponseEntity<List<SessionResponseDTO>> getTutorsSessions(@PathVariable String tutorId, HttpServletRequest request) {
        return ResponseEntity.ok(sessionService.getAllSessionsForTutor(tutorId, request));
    }

    @GetMapping("/tutors/requests/{tutorId}")
    public ResponseEntity<List<SessionResponseDTO>> getSessionRequests(@PathVariable String tutorId, HttpServletRequest request) {
        return ResponseEntity.ok(sessionService.getRequestedSessions(tutorId, request));
    }

    @GetMapping("/dashboard/{studentId}")
    public ResponseEntity<SessionDashboard> getSessionDashboard(@PathVariable String studentId, HttpServletRequest request) {
        return ResponseEntity.ok(sessionService.getStudentSessionDashboard(studentId, request));
    }


}