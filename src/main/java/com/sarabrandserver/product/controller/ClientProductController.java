package com.sarabrandserver.product.controller;

import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.response.DetailResponse;
import com.sarabrandserver.product.response.ProductResponse;
import com.sarabrandserver.product.service.ClientProductService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("api/v1/client/product")
@RequiredArgsConstructor
public class ClientProductController {

    private final ClientProductService clientProductService;

    /**
     * Returns a Page of ProductResponse objects
     * */
    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Page<ProductResponse> allProducts(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "currency", defaultValue = "ngn") String currency
    ) {
        SarreCurrency sc = SarreCurrency.valueOf(currency.toUpperCase());
        return this.clientProductService
                .allProductsByUUID("", sc, "", page, Math.min(size, 20));
    }

    /**
     * Returns a Page of ProductResponse objects
     * */
    @ResponseStatus(OK)
    @GetMapping(path = "/find", produces = APPLICATION_JSON_VALUE)
    public List<ProductResponse> search(
            @NotNull @RequestParam(name = "search") String search,
            @RequestParam(name = "currency", defaultValue = "ngn") String currency
    ) {
        SarreCurrency c = SarreCurrency.valueOf(currency.toUpperCase());
        return this.clientProductService.search(search, c);
    }

    /**
     * Returns a list of DetailResponse objects
     */
    @ResponseStatus(OK)
    @GetMapping(path = "/detail", produces = APPLICATION_JSON_VALUE)
    public List<DetailResponse> productDetailsByProductUUID(
            @NotNull @RequestParam(value = "product_id") String uuid,
            @RequestParam(value = "currency", defaultValue = "ngn") String currency
    ) {
        var c = SarreCurrency.valueOf(currency.toUpperCase());
        return this.clientProductService.productDetailsByProductUUID(uuid, c);
    }

}
