package dev.capstone.payment.dto;

import java.io.Serializable;

public record PayloadMapper(String name, String key, String colour) implements Serializable { }