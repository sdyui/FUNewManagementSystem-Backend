package com.funewmanagementsystem.exception;

import org.springframework.http.HttpStatus;

public class InvalidStateTransitionException extends AppException {
    public InvalidStateTransitionException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
