<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/dto/UpdateProductDTO.java
package dev.webserver.product.dto;
========
package dev.capstone.product.dto;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/dto/UpdateProductDTO.java

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

public record UpdateProductDTO(
        @JsonProperty(value = "product_id")
        @NotNull(message = "cannot be empty")
        String uuid,

        @JsonProperty(value = "name")
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String name,

        @Size(max = 1000, message = "max of 1000 letters")
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String desc,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String currency,

        @NotNull(message = "cannot be empty")
        BigDecimal price,

        @NotNull(message = "cannot be empty")
        String category,

        @JsonProperty(value = "category_id")
        @NotNull(message = "cannot be empty")
        Long categoryId,

        @NotNull(message = "cannot be null")
        Double weight
) implements Serializable { }