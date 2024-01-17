package com.sarabrandserver.product.repository;

import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.exception.CustomSqlException;
import com.sarabrandserver.product.entity.Product;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class ProductServiceImpl {

    private final JdbcClient jdbcClient;

    public Product save(@NotNull Product product) {
        String sql = """
        INSERT INTO product (
            uuid, name, description, default_image_key,
            category_id
        )
        VALUES (:uuid, :name, :description, :image, :catId);
        """;

        KeyHolder key = new GeneratedKeyHolder();

        jdbcClient.sql(sql)
                .param("uuid", product.getUuid())
                .param("name", product.getName())
                .param("description", product.getDescription())
                .param("image", product.getDefaultKey())
                .param("catId", product.getProductCategory().getCategoryId())
                .update(key);

        return findById(Objects.requireNonNull(key.getKey()).longValue())
                .orElseThrow(() -> new CustomSqlException("unable to create product " + product.getName()));
    }

    public Optional<Product> findById(@NotNull final Long productId) {
        return this.jdbcClient
                .sql("SELECT * FROM product p WHERE p.product_id = :id;")
                .param("id", productId)
                .query(Product.class)
                .optional();
    }

}
