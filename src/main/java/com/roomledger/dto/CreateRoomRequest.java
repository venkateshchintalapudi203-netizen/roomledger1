package com.roomledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoomRequest {
    @NotBlank private String name;
    private String description;
    private Double budget;
}
