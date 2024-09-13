package com.example.accountbot.exception;

import com.example.accountbot.dto.ErrorResponseDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Order(99)
    @ExceptionHandler(value = NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn(e);
        return new ResponseEntity<>(ErrorResponseDto.error("No static resource favicon.ico."), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Order(100)
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleException(Exception e) {
        log.warn(e);
        e.printStackTrace();
        return new ResponseEntity<>(ErrorResponseDto.error("something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
