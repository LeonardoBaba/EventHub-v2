package br.com.baba.eventHub.api.handler;

import br.com.baba.eventHub.core.dto.ErrorMessageDTO;
import br.com.baba.eventHub.core.dto.FieldErrorDTO;
import br.com.baba.eventHub.core.exceptions.EventException;
import br.com.baba.eventHub.core.exceptions.IdempotencyConflictException;
import br.com.baba.eventHub.core.exceptions.UserAlreadyExistsException;
import br.com.baba.eventHub.core.exceptions.UserNotFoundException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> handleGenericException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError()
                .body(new ErrorMessageDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorMessageDTO(HttpStatus.FORBIDDEN.value(), e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorMessageDTO(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials"));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> handleJWTVerificationException(JWTVerificationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorMessageDTO(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired token"));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorMessageDTO(HttpStatus.UNAUTHORIZED.value(), "Authentication failed"));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> handleEventException(EventException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorMessageDTO(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler
    public ResponseEntity<List<FieldErrorDTO>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var errors = e.getFieldErrors();
        return ResponseEntity.badRequest().body(errors.stream().map(FieldErrorDTO::new).toList());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessageDTO(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorMessageDTO(HttpStatus.CONFLICT.value(), e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> handleIdempotencyConflictException(IdempotencyConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorMessageDTO(HttpStatus.CONFLICT.value(), e.getMessage()));
    }
}
