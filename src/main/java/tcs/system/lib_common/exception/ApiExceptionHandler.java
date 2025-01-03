package tcs.system.lib_common.exception;

import jakarta.ws.rs.client.ResponseProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tcs.system.lib_common.exception.dto.ApiException;

import java.lang.reflect.InvocationTargetException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(value = {ApiExceptionStatusException.class})
  public ResponseEntity<Object> handleTechnicalException(ApiExceptionStatusException e) {
    HttpStatus httpStatus = HttpStatus.valueOf(e.getStatusCode());
    var apiException = new ApiException(e.getMessage(),e.getStatusCode(),httpStatus,e.getError(), ZonedDateTime.now(ZoneId.of("Z")));
    return new ResponseEntity<>(apiException, httpStatus);
  }
}
