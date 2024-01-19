package com.sarabrandserver.payment.response;

import com.sarabrandserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public record PaymentResponse(String pubKey, SarreCurrency currency, BigDecimal total) { }