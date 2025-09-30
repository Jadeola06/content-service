package com.flexydemy.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlashCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flashcard_set_id", nullable = false)
    private FlashCardSet flashCardSet;

    private String question;

    private String correctAnswer;

    private String type;

    @ElementCollection
    @CollectionTable(name = "flashcard_options", joinColumns = @JoinColumn(name = "flashcard_id"))
    @Column(name = "option_text")
    private List<String> options;

    public FlashCard(FlashCardSet flashCardSet, String question, String correctAnswer, String type, List<String> options) {
        this.flashCardSet = flashCardSet;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.type = type;
        this.options = options;
    }
}
