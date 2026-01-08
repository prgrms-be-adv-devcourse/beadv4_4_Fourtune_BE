package com.fourtune.auction.global.exception;

import com.fourtune.auction.global.common.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler
 * 전역 예외 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // TODO: 예외 핸들러 구현
    // @ExceptionHandler(BusinessException.class)
    // public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e)
    
    // @ExceptionHandler(EntityNotFoundException.class)
    // public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException e)
    
    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<ApiResponse<Void>> handleException(Exception e)
}

