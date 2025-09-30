package com.flexydemy.content.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends ServiceException {
    public ResourceNotFoundException(String resource, String id) {
        super(resource + " with ID " + id + " not found.");
    }

    public ResourceNotFoundException(String error) {
        super(error);
    }
}

