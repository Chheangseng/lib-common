package tcs.system.lib_common.keycloak;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak-properties")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakProperties {
  private String realm;
  private String clientId;
  private String clientSecret;
  private String baseUrl;
  private String tokenUrl;
  private String logoutUrl;
  private String jwkUrl;
}
