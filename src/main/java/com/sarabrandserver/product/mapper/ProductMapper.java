package com.sarabrandserver.product.mapper;

import java.math.BigDecimal;

public record ProductMapper(
        String productUUID,
        String productName,
        String description,
        String currency,
        BigDecimal price,
        String image,
        String categoryName
) {}