package com.sarabrandserver.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record Checkout (
        @JsonProperty("ship_cost")
        BigDecimal ship,
        @JsonProperty("tax_name")
        String tax,
        @JsonProperty("tax_rate")
        double rate,
        @JsonProperty("tax_total")
        BigDecimal taxTotal,
        BigDecimal total
) { }