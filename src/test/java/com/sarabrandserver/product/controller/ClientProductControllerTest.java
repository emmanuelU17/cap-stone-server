package com.sarabrandserver.product.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.product.repository.ProductDetailRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClientProductControllerTest extends AbstractIntegrationTest {

    @Value(value = "/${api.endpoint.baseurl}client/product")
    private String path;

    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductSkuRepo productSkuRepo;
    @Autowired
    private ProductDetailRepo productDetailRepo;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void before() {
        var category = categoryRepository
                .save(
                        ProductCategory.builder()
                                .name("category")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(category, 2, workerProductService);

        var clothes = categoryRepository
                .save(
                        ProductCategory.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(clothes, 5, workerProductService);
    }

    @AfterEach
    void after() {
        productSkuRepo.deleteAll();
        productDetailRepo.deleteAll();
        productRepo.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void search_functionality() throws Exception {
        char c = (char) ('a' + new Random().nextInt(26));
        this.MOCKMVC
                .perform(get(path + "/find")
                        .param("search", String.valueOf(c))
                        .param("currency", "usd")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void list_products_ngn_currency() throws Exception {
        this.MOCKMVC.perform(get(path)).andExpect(status().isOk());
    }

    @Test
    void list_products_usd_currency() throws Exception {
        this.MOCKMVC
                .perform(get(path).param("currency", "usd"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchProductDetails() throws Exception {
        var list = this.productRepo.findAll();
        assertFalse(list.isEmpty());
        String id = list.getFirst().getUuid();

        String[] arr = new String[3];
        Arrays.fill(arr, "0");

        this.MOCKMVC
                .perform(get(path + "/detail").param("product_id", id))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].variants").isArray())
                .andExpect(jsonPath("$[*].variants.length()").value(3))
                .andExpect(jsonPath("$[*].variants[*].inventory").value(hasItems(arr)));
    }

}