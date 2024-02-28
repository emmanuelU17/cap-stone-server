package com.sarabrandserver.payment.util;

public record WebhookMetaData(
        String email,
        String name,
        String phone,
        String address,
        String city,
        String state,
        String postcode,
        String country,
        String deliveryInfo,
        String referrer
) { }