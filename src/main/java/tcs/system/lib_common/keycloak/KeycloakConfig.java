package tcs.system.lib_common.keycloak;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tcs.system.lib_common.keycloak.exception.ExceptionFilter;

@Configuration
public class KeycloakConfig {
  private final KeycloakProperties properties;

  public KeycloakConfig(KeycloakProperties properties) {
    this.properties = properties;
  }

  @Bean
  public Keycloak keycloak() {
    Client client = ClientBuilder.newClient().register(new ExceptionFilter());
    return KeycloakBuilderFactory.builder(properties)
        .serverUrl(properties.getBaseUrl())
        .realm(properties.getRealm())
        .clientId(properties.getClientId())
        .clientSecret(properties.getClientSecret())
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .resteasyClient(client)
//        .resteasyClient(new ResteasyClientBuilderImpl().register(ExceptionFilter.class).connectionPoolSize(20).build())
        .build();
  }
}
