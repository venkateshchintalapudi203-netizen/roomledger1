package com.roomledger.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank String name;
    @NotBlank @Email String email;
    @NotBlank @Size(min = 6) String password;
}
