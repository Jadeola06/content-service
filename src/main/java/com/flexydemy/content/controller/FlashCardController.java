package com.flexydemy.content.controller;

import com.flexydemy.content.dto.FlashCardItemDTO;
import com.flexydemy.content.dto.FlashCardResponseDTO;
import com.flexydemy.content.dto.FlashCardSetRequestDTO;
import com.flexydemy.content.dto.FlashCardSetResponseDTO;
import com.flexydemy.content.service.FlashCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
public class FlashCardController {

    private final FlashCardService flashCardService;

    // Create a new FlashCardSet
    @PostMapping("/sets")
    public ResponseEntity<FlashCardSetResponseDTO> createFlashCardSet(@RequestBody FlashCardSetRequestDTO dto) {
        FlashCardSetResponseDTO response = flashCardService.createFlashCardSet(dto, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/sets/{id}")
    public ResponseEntity<Void> deleteFlashCardSet(@PathVariable String id, @RequestParam String tutorId) {
        flashCardService.deleteFlashCardSet(id, tutorId, null);
        return ResponseEntity.noContent().build();
    }

    // Get flashcard sets by title
    @GetMapping("/sets/title/{title}")
    public ResponseEntity<List<FlashCardSetResponseDTO>> getFlashCardSetsByTitle(@PathVariable String title) {
        List<FlashCardSetResponseDTO> response = flashCardService.getFlashCardSetsByTitle(title);
        return ResponseEntity.ok(response);
    }

    // Get flashcard sets by subject
    @GetMapping("/sets/subject/{subjectStr}")
    public ResponseEntity<List<FlashCardSetResponseDTO>> getFlashCardSetsBySubject(@PathVariable String subjectStr) {
        List<FlashCardSetResponseDTO> response = flashCardService.getFlashCardSetsBySubject(subjectStr);
        return ResponseEntity.ok(response);
    }

    // Get flashcard sets created by a user
    @GetMapping("/sets/user/{userId}")
    public ResponseEntity<List<FlashCardSetResponseDTO>> getFlashCardSetsCreatedByUser(@PathVariable String userId, HttpServletRequest request) {
        List<FlashCardSetResponseDTO> response = flashCardService.getFlashCardSetsCreatedByUser(userId, request);
        return ResponseEntity.ok(response);
    }

    // Get flashcard sets created by a tutor
    @GetMapping("/sets/tutor/{tutorId}")
    public ResponseEntity<List<FlashCardSetResponseDTO>> getFlashCardSetsCreatedByTutor(@PathVariable String tutorId) {
        List<FlashCardSetResponseDTO> response = flashCardService.getFlashCardSetsCreatedByTutor(tutorId);
        return ResponseEntity.ok(response);
    }

    // Get flashcard sets by course
    @GetMapping("/sets/course/{courseId}")
    public ResponseEntity<List<FlashCardSetResponseDTO>> getFlashCardSetsByCourse(@PathVariable String courseId) {
        List<FlashCardSetResponseDTO> response = flashCardService.getFlashCardSetsByCourse(courseId);
        return ResponseEntity.ok(response);
    }

    // Add a flashcard to a set
    @PostMapping("/sets/{setId}/cards")
    public ResponseEntity<FlashCardResponseDTO> addFlashCardToSet(
            @PathVariable String setId,
            @RequestParam String tutorId,
            @RequestBody FlashCardItemDTO dto) {

        FlashCardResponseDTO response = flashCardService.addFlashCardToSet(setId, tutorId, dto, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);  // HTTP 201
    }

    // Update a specific flashcard
    @PutMapping("/cards/{flashCardId}")
    public ResponseEntity<FlashCardResponseDTO> updateFlashCard(
            @PathVariable String flashCardId,
            @RequestParam String tutorId,
            @RequestBody FlashCardItemDTO dto) {

        FlashCardResponseDTO response = flashCardService.updateFlashCard(flashCardId, dto, tutorId, null);
        return ResponseEntity.ok(response);
    }


    // Delete a flashcard
    @DeleteMapping("/cards/{flashCardId}")
    public ResponseEntity<Void> deleteFlashCard(@PathVariable String flashCardId, @RequestParam String tutorId) {
        flashCardService.deleteFlashCard(flashCardId, tutorId, null);
        return ResponseEntity.noContent().build();  // HTTP 204 No Content
    }
}
