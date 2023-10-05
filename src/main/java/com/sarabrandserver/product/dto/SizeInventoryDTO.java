package com.sarabrandserver.product.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SizeInventoryDTO (
        @NotNull(message = "Please enter or choose product quantity")
        Integer qty,
        @NotNull(message = "Please enter or choose product size")
        @NotEmpty(message = "Please enter or choose product size")
        String size
) { }
