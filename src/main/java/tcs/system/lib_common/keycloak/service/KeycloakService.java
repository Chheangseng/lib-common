package tcs.system.lib_common.keycloak.service;


import jakarta.ws.rs.core.Response;
import java.util.*;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
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
import tcs.system.lib_common.dto.DtoKeyCloakAccount;
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

  public String createUser(DtoKeyCloakAccount dtoCreateAccount) {
    if (Objects.isNull(dtoCreateAccount)) {
      throw new ApiExceptionStatusException("unable to create user", 400);
    }
    Response response;
    var account = getUserRepresentation(dtoCreateAccount);
    response = this.resource.users().create(account);
    var userId = CreatedResponseUtil.getCreatedId(response);
    if (!userId.isEmpty()) {
      assignRealmRoleToUser(userId, dtoCreateAccount.getRole());
    }
    return userId;
  }
  public void updateUser (String keyCloakUserId, DtoKeyCloakAccount dtoCreateAccount){
    if (Objects.isNull(keyCloakUserId) || keyCloakUserId.isEmpty()){
      throw new ApiExceptionStatusException("User Id is required",400);
    }
    dtoCreateAccount.setId(keyCloakUserId);
    this.userResource(keyCloakUserId).update(getUserRepresentation(dtoCreateAccount));
    if (Objects.nonNull(dtoCreateAccount.getRole()) && !dtoCreateAccount.getRole().isEmpty()) {
      assignRealmRoleToUser(keyCloakUserId, dtoCreateAccount.getRole());
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
    if (Objects.isNull(roles) || roles.isEmpty()){
      return;
    }
    this.userResource(userId)
        .roles()
        .realmLevel()
        .add(
            roles.stream()
                .map(
                    role -> {
                      try {
                        return rolesResource().get(role).toRepresentation();
                      } catch (Exception exception) {
                        this.createRole(role);
                      }
                      return rolesResource().get(role).toRepresentation();
                    })
                .toList());
  }

  private UserRepresentation getUserRepresentation(DtoKeyCloakAccount dtoCreateAccount) {
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
    account.setEmailVerified(true);
    account.setUsername(dtoCreateAccount.getUsername());
    account.setRequiredActions(Collections.emptyList());
    if (Objects.nonNull(dtoCreateAccount.getId())){
      account.setId(dtoCreateAccount.getId());
    }
    if (Objects.nonNull(dtoCreateAccount.getAttributes())
        && !dtoCreateAccount.getAttributes().isEmpty()) {
      account.setAttributes(dtoCreateAccount.getAttributes());
    }
    account.setCreatedTimestamp(new Date().getTime());
    return account;
  }

  private void createRole(String role) {
    try {
      var roleRepresent = new RoleRepresentation();
      roleRepresent.setName(role);
      this.rolesResource().create(roleRepresent);
    } catch (Exception e) {
      throw new ApiExceptionStatusException("Identify service error", 400);
    }
  }
//  public Jwt getDecodeToken() {
//    Object authentication = SecurityContextHolder.getContext().getAuthentication();
//    if (Objects.isNull(authentication)) return null;
//    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    if (Objects.isNull(principal)) {
//      return null;
//    }
//    if (principal instanceof Jwt jwt) {
//      return jwt;
//    } else {
//      throw new ApiExceptionStatusException("Something went wrong invalid jwt token", 500);
//    }
//  }

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
