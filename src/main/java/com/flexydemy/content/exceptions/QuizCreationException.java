package com.flexydemy.content.exceptions;

public class QuizCreationException extends RuntimeException {
    public QuizCreationException(String message) {
        super(message);
    }

    public QuizCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
