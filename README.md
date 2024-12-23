- Application config
  + @Import({KeycloakConfig.class, KeycloakService.class, ApiExceptionHandler.class})
  + @EnableConfigurationProperties({KeycloakProperties.class, FileSystemProperties.class})

- application.yml

  + keycloak-properties:
    realm: ${KEYCLOAK_REALM}
    client-id: ${CLIENT_ID}
    client-secret: ${CLIENT_SECRET}
    base-url: ${KEYCLOAK_BASE_URL}
    token-url: ${KEYCLOAK_TOKEN_URL}
    logout-url: ${KEYCLOAK_LOG_OUT}
    jwk-url: ${KEYCLOAK_JWK_URL}

  + file-system-properties:
    base-driver-path: ${BASE_PATH}
    base-directory-folder: ${BASE_DIRECTORY_FOLDER}
