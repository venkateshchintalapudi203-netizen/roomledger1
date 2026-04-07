package com.roomledger.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InviteRequest {
    @NotBlank @Email private String email;
}
