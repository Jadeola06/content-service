package com.flexydemy.content.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class VideoProgressException extends ServiceException {
    public VideoProgressException(String message) {
        super(message);
    }
}
