package com.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ProductDetailDTO(
        @JsonProperty(value = "product_id")
        @NotNull(message = "UUID cannot be empty")
        @NotEmpty(message = "UUID cannot be empty")
        String uuid,

        @NotNull(message = "Please choose if product should be visible")
        Boolean visible,

        @NotNull(message = "Please enter or choose product colour")
        @NotEmpty(message = "Please enter or choose product colour")
        String colour,

        @JsonProperty(value = "sizeInventory")
        @NotNull(message = "Size or Inventory cannot be empty")
        SizeInventoryDTO[] sizeInventory
) { }