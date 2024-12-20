package tcs.system.lib_common.keycloak.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class ExceptionFilter implements ClientResponseFilter {
  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
      throws IOException {
    final var status = HttpStatus.valueOf(responseContext.getStatus());
    if (status.isError()) {
      final var stream = responseContext.getEntityStream();
      final var jsonNode = new ObjectMapper().readTree(stream);
      System.out.println("----------------------- keycloak error -----------------------");
      System.out.println(jsonNode);
      System.out.println("--------------------------------------------------------------");
    }
  }
}
