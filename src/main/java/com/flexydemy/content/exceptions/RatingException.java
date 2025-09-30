package com.flexydemy.content.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RatingException extends ServiceException {
    public RatingException(String message) {
        super(message);
    }
}
