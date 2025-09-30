package com.flexydemy.content.service;

import com.flexydemy.content.dto.FlashCardItemDTO;
import com.flexydemy.content.dto.FlashCardResponseDTO;
import com.flexydemy.content.dto.FlashCardSetRequestDTO;
import com.flexydemy.content.dto.FlashCardSetResponseDTO;
import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.exceptions.AuthorizationException;
import com.flexydemy.content.exceptions.BadRequestException;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.*;
import com.flexydemy.content.dto.UserDto;
import com.flexydemy.content.repository.*;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashCardService {

    private final FlashCardSetRepository flashCardSetRepository;
    private final FlashCardRepository flashCardRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final TutorRepository tutorRepository;
    private final UserClient userRepository;

    public FlashCardSetResponseDTO createFlashCardSet(FlashCardSetRequestDTO dto, UserDto admin) {
        if (dto == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Title is null");
        }
        if (dto.getCourseId().isBlank()) {
            throw new BadRequestException("Course Id is null");
        }
        if (dto.getClassCategory().isBlank()) {
            throw new BadRequestException("Class Category is null or empty");
        }
        if (dto.getTutorId().isBlank() && admin == null) {
            throw new BadRequestException("Tutor id is null or empty and is required for this request");
        }
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Lesson lesson = lessonRepository.findById(dto.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));


        FlashCardSet set = new FlashCardSet();
        set.setTitle(dto.getTitle());
        set.setCourse(course);
        if (lesson != null) {
            set.setLesson(lesson);
        }
        set.setSubject(Class_Categories.valueOf(dto.getClassCategory()));

        if (admin == null) {
            Tutor tutor = tutorRepository.findById(dto.getTutorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));
            set.setCreatedByTutor(tutor);
            //set.setCreatedByUser(tutor.getFirstName() + " " + tutor.getLastName());
        }else{
            set.setCreatedByUser(admin.getFirstName() + " " + admin.getLastName());
        }


        List<FlashCard> flashCards = dto.getFlashCards().stream().map(item -> {
            FlashCard card = new FlashCard();
            card.setQuestion(item.getQuestion());
            card.setCorrectAnswer(item.getCorrectAnswer());
            card.setOptions(item.getOptions());
            card.setFlashCardSet(set);
            return card;
        }).collect(Collectors.toList());

        set.setFlashCards(flashCards);
        FlashCardSet savedSet = flashCardSetRepository.save(set);

        return toResponseDTO(savedSet);
    }

    public List<FlashCardSetResponseDTO> getFlashCardSetsByTitle(String title) {
        return flashCardSetRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<FlashCardSetResponseDTO> getFlashCardSetsBySubject(String subjectStr) {
        try {
            Class_Categories subject = Class_Categories.valueOf(subjectStr.toUpperCase());
            return flashCardSetRepository.findBySubject(subject)
                    .stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid subject: " + subjectStr);
        }
    }

    public List<FlashCardSetResponseDTO> getFlashCardSetsCreatedByUser(String userId, HttpServletRequest request) {
        String token = Utils.getToken(request);

        UserDto user = userRepository.findById(userId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student", userId)))
                .block();

        List<FlashCardSet> sets = flashCardSetRepository.findByCreatedByUser(user.getUserId());

        return sets.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<FlashCardSetResponseDTO> getFlashCardSetsCreatedByTutor(String tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + tutorId));

        List<FlashCardSet> sets = flashCardSetRepository.findByCreatedByTutor(tutor);

        return sets.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<FlashCardSetResponseDTO> getFlashCardSetsByCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        List<FlashCardSet> sets = flashCardSetRepository.findByCourse(course);

        return sets.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public FlashCardResponseDTO addFlashCardToSet(String setId, String tutorId, FlashCardItemDTO dto, UserDto admin) {
        if (setId == null || setId.isBlank()) {
            throw new BadRequestException("Set Id is null or empty");
        }

        if (dto == null) {
            throw new BadRequestException("FlashCard data is null");
        }

        if (dto.getQuestion() == null || dto.getQuestion().isBlank()) {
            throw new BadRequestException("Question cannot be null or blank");
        }

        if (dto.getCorrectAnswer() == null || dto.getCorrectAnswer().isBlank()) {
            throw new BadRequestException("Correct answer cannot be null or blank");
        }

        if (dto.getOptions() == null || dto.getOptions().isEmpty()) {
            throw new BadRequestException("Options cannot be null or empty");
        }

        if (!dto.getOptions().contains(dto.getCorrectAnswer())) {
            throw new BadRequestException("Correct answer must be one of the options");
        }

        if (admin == null && (tutorId == null || tutorId.isBlank())) {
            throw new BadRequestException("Incomplete request. Tutor Id and Admin credentials are missing");
        }

        FlashCardSet flashCardSet = flashCardSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("FlashCardSet not found with id: " + setId));

        if (!flashCardSet.getCreatedByTutor().getTutorId().equals(tutorId) && admin == null) {
            throw new AuthorizationException("You do not have permission to add to this flashcard set");
        }

        FlashCard flashCard = new FlashCard();
        flashCard.setQuestion(dto.getQuestion().trim());
        flashCard.setCorrectAnswer(dto.getCorrectAnswer().trim());
        flashCard.setOptions(dto.getOptions());
        flashCard.setFlashCardSet(flashCardSet);  // Associate with the set

        if (admin != null) {
            flashCardSet.setUpdatedBy(admin.getFirstName() + " " + admin.getLastName());
        }else{
            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new RuntimeException("Tutor not found with id: " + setId));
            //flashCardSet.setUpdatedBy(tutor.getFirstName() + " " + tutor.getLastName());

        }

        FlashCard savedFlashCard = flashCardRepository.save(flashCard);
        return toResponseDTO(savedFlashCard);
    }

    public FlashCardResponseDTO updateFlashCard(String flashCardId, FlashCardItemDTO dto, String tutorId, UserDto admin) {
        if (dto == null) {
            throw new BadRequestException("FlashCard data is null");
        }

        if (dto.getQuestion() == null || dto.getQuestion().isBlank()) {
            throw new BadRequestException("Question cannot be null or blank");
        }

        if (dto.getCorrectAnswer() == null || dto.getCorrectAnswer().isBlank()) {
            throw new BadRequestException("Correct answer cannot be null or blank");
        }

        if (dto.getOptions() == null || dto.getOptions().isEmpty()) {
            throw new BadRequestException("Options cannot be null or empty");
        }

        if (!dto.getOptions().contains(dto.getCorrectAnswer())) {
            throw new BadRequestException("Correct answer must be one of the options");
        }

        if (admin == null && (tutorId == null || tutorId.isBlank())) {
            throw new BadRequestException("Incomplete request. Tutor Id and Admin credentials are missing");
        }

        FlashCard flashCard = flashCardRepository.findById(flashCardId)
                .orElseThrow(() -> new ResourceNotFoundException("FlashCard not found with id: " + flashCardId));

        FlashCardSet flashCardSet = flashCard.getFlashCardSet();

        if (!flashCardSet.getCreatedByTutor().getTutorId().equals(tutorId) && admin == null) {
            throw new AuthorizationException("You do not have permission to update this flashcard");
        }

        flashCard.setQuestion(dto.getQuestion().trim());
        flashCard.setCorrectAnswer(dto.getCorrectAnswer().trim());
        flashCard.setOptions(dto.getOptions());

        if (admin != null) {
            flashCardSet.setUpdatedBy(admin.getFirstName() + " " + admin.getLastName());
        } else {
            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + tutorId));
            //flashCardSet.setUpdatedBy(tutor.getFirstName() + " " + tutor.getLastName());
        }

        FlashCard updatedFlashCard = flashCardRepository.save(flashCard);
        return toResponseDTO(updatedFlashCard);
    }

    public void deleteFlashCard(String flashCardId, String tutorId, UserDto admin) {
        if (admin == null && (tutorId == null || tutorId.isBlank())) {
            throw new BadRequestException("Incomplete request. Tutor Id and Admin credentials are missing");
        }

        FlashCard flashCard = flashCardRepository.findById(flashCardId)
                .orElseThrow(() -> new ResourceNotFoundException("FlashCard not found with id: " + flashCardId));

        FlashCardSet flashCardSet = flashCard.getFlashCardSet();

        if (!flashCardSet.getCreatedByTutor().getTutorId().equals(tutorId) && admin == null) {
            throw new AuthorizationException("You do not have permission to delete this flashcard");
        }

        if (admin != null) {
            flashCardSet.setUpdatedBy(admin.getFirstName() + " " + admin.getLastName());
        } else {
            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + tutorId));
            //flashCardSet.setUpdatedBy(tutor.getFirstName() + " " + tutor.getLastName());
        }

        flashCardRepository.delete(flashCard);
    }

    public void deleteFlashCardSet(String flashCardSetId, String tutorId, UserDto admin) {
        if (admin == null && (tutorId == null || tutorId.isBlank())) {
            throw new BadRequestException("Incomplete request. Tutor Id and Admin credentials are missing");
        }

        FlashCardSet flashCardSet = flashCardSetRepository.findById(flashCardSetId)
                .orElseThrow(() -> new ResourceNotFoundException("FlashCard Set not found with id: " + flashCardSetId));

        if (!flashCardSet.getCreatedByTutor().getTutorId().equals(tutorId) && admin == null) {
            throw new AuthorizationException("You do not have permission to delete this flashcard set");
        }

        // Optional: Set audit log
        if (admin != null) {
            flashCardSet.setUpdatedBy(admin.getFirstName() + " " + admin.getLastName());
        } else {
            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + tutorId));
            //flashCardSet.setUpdatedBy(tutor.getFirstName() + " " + tutor.getLastName());
        }

        flashCardSetRepository.delete(flashCardSet);
    }



    private FlashCardSetResponseDTO toResponseDTO(FlashCardSet set) {
        FlashCardSetResponseDTO dto = new FlashCardSetResponseDTO();
        dto.setId(set.getId());
        dto.setTitle(set.getTitle());
        dto.setCourseTitle(set.getCourse().getCourseTitle());
        dto.setLessonTitle(set.getLesson() != null ? set.getLesson().getTitle() : null);
        dto.setCreatedBy(set.getCreatedByUser());
        dto.setSubject(set.getSubject().getName());
        dto.setFlashCardsCount(set.getFlashCards().size());
        dto.setFlashCards(set.getFlashCards().stream().map(card -> {
            FlashCardItemDTO item = new FlashCardItemDTO();
            item.setQuestion(card.getQuestion());
            item.setCorrectAnswer(card.getCorrectAnswer());
            item.setOptions(card.getOptions());
            return item;
        }).collect(Collectors.toList()));
        return dto;
    }

    private FlashCardResponseDTO toResponseDTO(FlashCard flashCard) {
        FlashCardResponseDTO dto = new FlashCardResponseDTO();
        dto.setId(flashCard.getId());
        dto.setQuestion(flashCard.getQuestion());
        dto.setCorrectAnswer(flashCard.getCorrectAnswer());
        dto.setOptions(flashCard.getOptions());


        return dto;
    }




}
