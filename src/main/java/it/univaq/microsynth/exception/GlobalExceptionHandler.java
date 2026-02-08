package it.univaq.microsynth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors for method arguments. This method captures exceptions of type MethodArgumentNotValidException, which are thrown when validation on an argument annotated with @Valid fails. It extracts the field errors from the exception and constructs a map of field names to error messages, which is then returned in the response body with a BAD_REQUEST status.
     *
     * @param ex The MethodArgumentNotValidException containing details about the validation errors.
     * @return A ResponseEntity containing a map of field errors and an HTTP status of BAD_REQUEST.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handle IllegalArgumentException exceptions. This method captures exceptions of type IllegalArgumentException, which are typically thrown when a method receives an argument that is not valid. It returns a response with a BAD_REQUEST status and a message indicating the nature of the error.
     *
     * @param ex The IllegalArgumentException containing details about the error.
     * @return A ResponseEntity containing an error message and an HTTP status of BAD_REQUEST.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parameters error: " + ex.getMessage());
    }

    /**
     * Handle generic exceptions. This method captures any exceptions of type Exception that are not specifically handled by other methods. It returns a response with an INTERNAL_SERVER_ERROR status and a message indicating that an internal error occurred, along with the exception message for debugging purposes.
     *
     * @param ex The Exception containing details about the error.
     * @return A ResponseEntity containing an error message and an HTTP status of INTERNAL_SERVER_ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal errors: " + ex.getMessage());
    }
}