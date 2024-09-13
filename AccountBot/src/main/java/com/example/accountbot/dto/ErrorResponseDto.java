package com.example.accountbot.dto;

import lombok.Data;

@Data
public class ErrorResponseDto<T> {

    private T error;

    private ErrorResponseDto(T error) {
        this.error = error;
    }

    public static <T> ErrorResponseDto<T> error(T error) {
        return new ErrorResponseDto<>(error);
    }

}
