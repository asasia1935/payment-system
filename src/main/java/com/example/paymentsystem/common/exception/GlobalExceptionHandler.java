package com.example.paymentsystem.common.exception;

import com.example.paymentsystem.merchant.MerchantNotActiveException;
import com.example.paymentsystem.merchant.MerchantNotFoundException;
import com.example.paymentsystem.payment.DuplicatePaymentOrderException;
import com.example.paymentsystem.payment.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MerchantNotFoundException.class)
    public ResponseEntity<Void> handleMerchantNotFound(MerchantNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MerchantNotActiveException.class)
    public ResponseEntity<Void> handleMerchantNotActive(MerchantNotActiveException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(DuplicatePaymentOrderException.class)
    public ResponseEntity<Void> handleDuplicatePaymentOrder(DuplicatePaymentOrderException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Void> handlePaymentNotFoundException(PaymentNotFoundException ex) {

        return ResponseEntity.notFound().build();
    }
}
