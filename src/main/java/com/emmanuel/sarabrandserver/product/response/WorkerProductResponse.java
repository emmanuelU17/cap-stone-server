package com.emmanuel.sarabrandserver.product.response;

import com.emmanuel.sarabrandserver.product.projection.WorkerProductPojo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WorkerProductResponse implements WorkerProductPojo {
    private String name;
    private String desc;
    private BigDecimal price;
    private String currency;
    private String sku;
    private boolean status;
    private String size;
    private int quantity;
    @JsonProperty(value = "url")
    private String imageUrl;
    private String colour;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    public BigDecimal getPrice() {
        return this.price;
    }

    @Override
    public String getCurrency() {
        return this.currency;
    }

    @Override
    public String getSku() {
        return this.sku;
    }

    @Override
    public boolean getStatus() {
        return this.status;
    }

    @Override
    public String getSizes() {
        return this.size;
    }

    @Override
    public int getQuantity() {
        return this.quantity;
    }

    @Override
    public String getImage() {
        return this.imageUrl;
    }

    @Override
    public String getColour() {
        return this.colour;
    }
}