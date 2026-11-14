package com.example.paymentsystem.common.exception;

import com.example.paymentsystem.merchant.MerchantNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MerchantNotFoundException.class)
    public ResponseEntity<Void> handleMerchantNotFound(MerchantNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}
