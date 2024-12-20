package tcs.system.lib_common.service;

import jakarta.ws.rs.NotFoundException;

import java.util.*;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import tcs.system.lib_common.dto.DtoCreateAccount;
import tcs.system.lib_common.exception.ApiExceptionStatusException;
import tcs.system.lib_common.keycloak.KeycloakProperties;

@Service
public class KeycloakService {
  private final KeycloakProperties properties;
  private final RealmResource resource;

  public KeycloakService(KeycloakProperties properties, Keycloak keycloak) {
    this.properties = properties;
    this.resource = keycloak.realm(properties.getRealm());
  }

  public RolesResource rolesResource() {
    return this.resource.roles();
  }

  public UsersResource usersResource() {
    return this.resource.users();
  }

  public UserResource userResource(String userId) {
    return this.resource.users().get(userId);
  }

  public List<UserRepresentation> isUserExist(String email) {
    try {
      return this.usersResource().searchByEmail(email, true);
    } catch (Exception exception) {
      return Collections.emptyList();
    }
  }

  public AccessTokenResponse login(String username, String password) {
    var form = this.buildForm();
    form.add(OAuth2Constants.GRANT_TYPE, OAuth2Constants.PASSWORD);
    form.add(OAuth2Constants.USERNAME, username);
    form.add(OAuth2Constants.PASSWORD, password);
    return new RestTemplate()
        .postForEntity(
            properties.getTokenUrl(),
            new HttpEntity<>(form, this.getUrlFormEncodedHeader()),
            AccessTokenResponse.class)
        .getBody();
  }

  public void createUser(DtoCreateAccount dtoCreateAccount) {
    if (Objects.isNull(dtoCreateAccount)) {
      throw new ApiExceptionStatusException("Object is null", 400);
    }
    var account = getUserRepresentation(dtoCreateAccount);
    try {
      this.resource.users().create(account);
    } catch (Exception e) {
      throw new ApiExceptionStatusException("unable to create user", 400);
    }
  }

  public void logout(String refreshToken) {
    if (!StringUtils.hasText(refreshToken)) {
      throw new ApiExceptionStatusException("Refresh token is empty", 400);
    }
    var form = this.buildForm();
    form.add(OAuth2Constants.REFRESH_TOKEN, refreshToken);
    new RestTemplate()
        .postForEntity(
            properties.getLogoutUrl(),
            new HttpEntity<>(form, this.getUrlFormEncodedHeader()),
            Void.class);
  }

  public void resetPassword(String email, String password) {
    var userList = isUserExist(email);
    if (userList.isEmpty()) {
      throw new ApiExceptionStatusException("Invalid email", 400);
    }
    var userAccount = userList.get(0);
    var credential = new CredentialRepresentation();
    credential.setTemporary(false);
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(password);
    userAccount.setCredentials(Collections.singletonList(credential));
    userResource(userAccount.getId()).update(userAccount);
  }

  public void deleteUserByEmail(String email) {
    var usersFounded = isUserExist(email);
    if (Objects.isNull(usersFounded) || usersFounded.isEmpty()) {
      return;
    }
    var userEntity = usersFounded.get(0);
    userResource(userEntity.getId()).remove();
  }

  public AccessTokenResponse renewedAccessToken(final String refreshToken) {
    var form = this.buildForm();
    form.add(OAuth2Constants.REFRESH_TOKEN, refreshToken);
    form.add(OAuth2Constants.GRANT_TYPE, OAuth2Constants.REFRESH_TOKEN);
    return new RestTemplate()
        .postForEntity(
            properties.getTokenUrl(),
            new HttpEntity<>(form, this.getUrlFormEncodedHeader()),
            AccessTokenResponse.class)
        .getBody();
  }

  public void deleteAllUser() {
    usersResource()
        .list()
        .forEach(
            userRepresentation -> {
              userResource(userRepresentation.getId()).remove();
            });
  }

  public void assignRealmRoleToUser(String userId, List<String> roles) {
    this.userResource(userId)
        .roles()
        .realmLevel()
        .add(
            roles.stream()
                .map(
                    role -> {
                      try {
                        return rolesResource().get(role).toRepresentation();
                      } catch (NotFoundException exception) {
                        var roleRepresent = new RoleRepresentation();
                        roleRepresent.setName(role);
                        rolesResource().create(roleRepresent);
                      }
                      return rolesResource().get(role).toRepresentation();
                    })
                .toList());
  }

  private UserRepresentation getUserRepresentation(DtoCreateAccount dtoCreateAccount) {
    var account = new UserRepresentation();
    var credential = new CredentialRepresentation();
    credential.setTemporary(false);
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(dtoCreateAccount.getPassword());
    account.setEmail(dtoCreateAccount.getEmail());
    account.setCredentials(Collections.singletonList(credential));
    account.setFirstName(dtoCreateAccount.getFirstname());
    account.setLastName(dtoCreateAccount.getLastName());
    account.setEnabled(true);
    account.setEmailVerified(false);
    account.setUsername(dtoCreateAccount.getEmail());
    if (Objects.nonNull(dtoCreateAccount.getAttributes())
        && !dtoCreateAccount.getAttributes().isEmpty()) {
      account.setAttributes(dtoCreateAccount.getAttributes());
    }
    if (Objects.nonNull(dtoCreateAccount.getRole()) && !dtoCreateAccount.getRole().isEmpty()) {
      dtoCreateAccount.getRole().forEach(this::createRoleIfNotExist);
    }
    account.setCreatedTimestamp(new Date().getTime());
    return account;
  }

  private void createRoleIfNotExist(String role) {
    try {
      var response = this.rolesResource().get(role).toRepresentation();
    } catch (NotFoundException exception) {
      var roleRepresent = new RoleRepresentation();
      roleRepresent.setName(role);
      this.rolesResource().create(roleRepresent);
    }
  }

  private MultiValueMap<String, String> buildForm() {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add(OAuth2Constants.CLIENT_ID, properties.getClientId());
    form.add(OAuth2Constants.CLIENT_SECRET, properties.getClientSecret());
    return form;
  }

  private HttpHeaders getUrlFormEncodedHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    return headers;
  }
}
