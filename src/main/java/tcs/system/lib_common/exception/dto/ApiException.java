package tcs.system.lib_common.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Data
public class ApiException {
  private final String massage;
  private final int statusCode;
  private final HttpStatus status;
  private final ZonedDateTime zonedDateTime;
}
