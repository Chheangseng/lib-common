package tcs.system.lib_common.keycloak;

import lombok.NoArgsConstructor;
import org.keycloak.admin.client.KeycloakBuilder;

@NoArgsConstructor
public class KeycloakBuilderFactory {
  public static KeycloakBuilder builder(KeycloakProperties properties) {
    return KeycloakBuilder.builder()
        .clientId(properties.getClientId())
        .clientSecret(properties.getClientSecret())
        .realm(properties.getRealm())
        .serverUrl(properties.getBaseUrl());
  }
}
