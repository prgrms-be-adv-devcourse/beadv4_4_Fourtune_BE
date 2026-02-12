package com.fourtune.common.global.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API Response
 * 공통 API 응답 형식
 */
@NoArgsConstructor
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    
    private boolean success;
    private T data;
    private String message;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}

