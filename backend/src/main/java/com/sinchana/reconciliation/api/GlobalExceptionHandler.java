package com.sinchana.reconciliation.api;

import com.sinchana.reconciliation.exception.DataReadException;
import com.sinchana.reconciliation.exception.ReconciliationException;
import com.sinchana.reconciliation.exception.UnsupportedFileTypeException;
import com.sinchana.reconciliation.exception.ReportNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({ReconciliationException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ApiError> handleDomainException(Exception exception) {
        if (exception instanceof UnsupportedFileTypeException) return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception);
        if (exception instanceof ReportNotFoundException) return error(HttpStatus.NOT_FOUND, exception);
        if (exception instanceof DataReadException) return error(HttpStatus.UNPROCESSABLE_ENTITY, exception);
        return error(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleUploadLimit(MaxUploadSizeExceededException exception) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, exception);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, Exception exception) {
        String code = exception instanceof ReconciliationException domain ? domain.code() : "BAD_REQUEST";
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), code, exception.getMessage()));
    }

    public record ApiError(Instant timestamp, int status, String code, String message) {}
}
