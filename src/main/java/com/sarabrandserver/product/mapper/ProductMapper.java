package com.sarabrandserver.product.mapper;

import com.sarabrandserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public record ProductMapper(
        String productUUID,
        String productName,
        String description,
        SarreCurrency currency,
        BigDecimal price,
        String image,
        String categoryName
) {}