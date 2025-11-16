package com.vsa.model.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Data
public class AddItemRequest {

    private String userId;

    @NotBlank
    private String name;

    @Min(1)
    private int quantity = 1;

    private String unit;

    private String category;

    private Double price;
}
