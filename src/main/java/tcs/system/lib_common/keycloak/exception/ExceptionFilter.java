package tcs.system.lib_common.keycloak.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import org.springframework.http.HttpStatus;
import tcs.system.lib_common.exception.ApiExceptionStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ExceptionFilter implements ClientResponseFilter {
  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
      throws IOException {
    final var status = HttpStatus.valueOf(responseContext.getStatus());
    if (status.isError()) {
      final var stream = responseContext.getEntityStream();
      final var bytes = stream.readAllBytes();
      final var jsonNode = new ObjectMapper().readTree(bytes);
      System.out.println("----------------------- keycloak error -----------------------");
      System.out.println(jsonNode);
      System.out.println("--------------------------------------------------------------");
      responseContext.setEntityStream(new ByteArrayInputStream(bytes));
      throw new ApiExceptionStatusException("keycloak error",400);
    }
  }
}
