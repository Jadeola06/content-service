package com.flexydemy.content.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UserRetrievalException extends ServiceException{
    public UserRetrievalException(String message) {
        super(message);
    }

    public UserRetrievalException(String resource, String id) {
        super(resource + " with ID " + id + " failed to be retrieved from auth service");
    }

}
