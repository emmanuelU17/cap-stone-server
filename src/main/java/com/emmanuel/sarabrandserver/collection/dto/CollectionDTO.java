package com.emmanuel.sarabrandserver.collection.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CollectionDTO {
    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    private String name;
    @NotNull(message = "cannot be empty")
    private Boolean visible;
}
