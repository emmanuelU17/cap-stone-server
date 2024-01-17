package com.sarabrandserver.product.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryServiceImpl;
import com.sarabrandserver.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ProductServiceImplTest extends AbstractRepositoryTest {

    @Autowired
    private ProductServiceImpl productService;
    @Autowired
    private CategoryServiceImpl categoryService;

    @Test
    void save() {
        assertDoesNotThrow(() -> {
            var category = categoryService
                    .save(
                            ProductCategory
                                    .builder()
                                    .name("category")
                                    .isVisible(true)
                                    .parentCategory(null)
                                    .categories(new HashSet<>())
                                    .product(new HashSet<>())
                                    .build()
                    );

            var user = productService
                    .save(
                            Product.builder()
                                    .uuid(UUID.randomUUID().toString())
                                    .name("product 1")
                                    .description(new Faker().lorem().fixedString(500))
                                    .defaultKey("unique-image-key")
                                    .productCategory(category)
                                    .productDetails(new HashSet<>())
                                    .priceCurrency(new HashSet<>())
                                    .build()
                    );

            assertTrue(category.isVisible());
            assertNotNull(category.getCategoryId());
            assertEquals("product 1", user.getName());
            assertNotNull(user.getProductId());
        });
    }

}