package com.ledger.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "username must be 3-50 characters")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 64, message = "password must be 6-64 characters")
    private String password;

    @Email(message = "email must be valid")
    @Size(max = 100, message = "email is too long")
    private String email;
}
