package com.booking.bookingservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DomainException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ProblemDetail handleDomain(DomainException ex) {
    var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    pd.setTitle("Booking rule violation");
    pd.setDetail(ex.getMessage());
    return pd;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Validation error");
    pd.setDetail(ex.getBindingResult().toString());
    return pd;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleConstraint(ConstraintViolationException ex) {
    var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Constraint violation");
    pd.setDetail(ex.getMessage());
    return pd;
  }
}
