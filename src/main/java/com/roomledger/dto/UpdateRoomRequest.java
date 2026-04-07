package com.roomledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoomRequest {
    @NotBlank private String name;
    private String description;
    // budget removed — use monthly budget endpoint instead
}