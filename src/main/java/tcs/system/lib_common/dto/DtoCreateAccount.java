package tcs.system.lib_common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.*;

@Data
public class DtoCreateAccount {
    @NotNull(message = "username is required")
    @NotBlank(message = "username is required")
    private String username;
    @NotNull(message = "password is required")
    @NotBlank(message = "password is required")
    private String password;
    @NotNull(message = "email is required")
    @NotBlank(message = "email is required")
    @Email(message = "invalid email")
    private String email;
    private String firstname;
    private String lastName;
    private Map<String , List<String>> attributes;
    private List<String> role;

    public Map<String, List<String>> getAttributes() {
        if (Objects.isNull(this.attributes)){
            this.attributes =  new HashMap<>();
        }
        return this.attributes;
    }

    public List<String> getRole() {
        if (Objects.isNull(this.role)){
            return new ArrayList<>();
        }
        return this.role;
    }
    public void addAttributes(String key, String value) {
        // Get the list associated with the key, or create a new one if it doesn't exist
        List<String> values = this.getAttributes().computeIfAbsent(key, k -> new ArrayList<>());
        // Add the value to the list
        values.add(value);
    }
}
