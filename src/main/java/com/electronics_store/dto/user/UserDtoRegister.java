package com.electronics_store.dto.user;

import com.electronics_store.constraint.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@PasswordMatches
public class UserDtoRegister {
    private Long id;

    @Size(min = 6, max = 30, message = "Full Name must be between 6 and 30 characters")
    @NotBlank(message = "Cannot be left blank")
    private String fullname;

    @NotBlank(message = "Cannot be left blank.")
    @Size(min = 6, max = 20, message = "Username must be between 6 and 30 characters")
    private String username;

    @NotBlank(message = "Cannot be left blank.")
    @Size(min = 8, message = "Password must have at least 8 characters.")
    private String password;

    @NotBlank(message = "Cannot be left blank.")
    private String confirmPassword;
}
