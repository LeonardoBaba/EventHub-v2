package br.com.baba.eventHub.api.handler;

import br.com.baba.eventHub.core.dto.ErrorMessageDTO;
import br.com.baba.eventHub.core.dto.FieldErrorDTO;
import br.com.baba.eventHub.core.exceptions.EventException;
import br.com.baba.eventHub.core.exceptions.UserAlreadyExistsException;
import br.com.baba.eventHub.core.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler
    public ResponseEntity handleGenericException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().build();
    }

    @ExceptionHandler
    public ResponseEntity handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorMessageDTO(HttpStatus.FORBIDDEN.value(), e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorMessageDTO(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity handleEventException(EventException e) {
        return ResponseEntity.badRequest().body(new ErrorMessageDTO(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity handleEventException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler
    public ResponseEntity handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var errors = e.getFieldErrors();
        return ResponseEntity.badRequest().body(errors.stream().map(FieldErrorDTO::new).toList());
    }

    @ExceptionHandler
    public ResponseEntity handleUserException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessageDTO(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity handleUserException(NoResourceFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler
    public ResponseEntity handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorMessageDTO(HttpStatus.CONFLICT.value(), e.getMessage()));
    }

}
