package tcs.system.lib_common.exception;

import lombok.Getter;

@Getter
public class ApiExceptionStatusException extends RuntimeException {
  private final int statusCode;
  private final Object error;


  public ApiExceptionStatusException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
    this.error = null;
  }
  public ApiExceptionStatusException(String message,int statusCode, Object error){
    super(message);
    this.statusCode = statusCode;
    this.error = error;
  }
}
