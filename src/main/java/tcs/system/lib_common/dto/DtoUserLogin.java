package tcs.system.lib_common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DtoUserLogin {
    @NotNull(message = "username is required")
    @NotBlank(message = "username is required")
    private String username;
    @NotNull(message = "password is required")
    @NotBlank(message = "password is required")
    private String password;
}
