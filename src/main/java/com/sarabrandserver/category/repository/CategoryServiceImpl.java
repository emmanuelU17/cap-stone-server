package com.sarabrandserver.category.repository;

import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.mapper.CategoryMapper;
import com.sarabrandserver.exception.CustomSqlException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl {

    private final JdbcClient jdbcClient;

    public ProductCategory save(@NotNull ProductCategory category) {
        String sql = """
        INSERT INTO product_category (
            name, is_visible, parent_category_id
        )
        VALUES (:name, :status, :parentId);
        """;

        KeyHolder key = new GeneratedKeyHolder();

        jdbcClient.sql(sql)
                .param("name", category.getName())
                .param("status", category.isVisible() ? 1 : 0)
                .param("parentId", category.getParentCategory() == null
                        ? null : category.getParentCategory().getCategoryId()
                )
                .update(key);

        return findById(Objects.requireNonNull(key.getKey()).longValue())
                .orElseThrow(() -> new CustomSqlException("unable to create category " + category.getName()));
    }

    public Optional<ProductCategory> findById(@NotNull final Long categoryId) {
        return this.jdbcClient
                .sql("SELECT * FROM product_category p WHERE p.category_id = :id;")
                .param("id", categoryId)
                .query(ProductCategory.class)
                .optional();
    }

    public List<CategoryMapper> allCategories() {
        String sql = """
        SELECT
        c.category_id AS id,
        c.name AS name,
        c.is_visible AS status,
        c.parent_category_id AS parent
        FROM product_category c;
        """;
        return jdbcClient.sql(sql)
                .query(CategoryMapper.class)
                .list();
    }

    public Optional<ProductCategory> findByName(@NotNull final String name) {
        return this.jdbcClient
                .sql("SELECT * FROM product_category p WHERE p.name = :name;")
                .param("name", name)
                .query(ProductCategory.class)
                .optional();
    }

    /**
     * Using native sql query, method retrieves {@code ProductCategory}
     * by {@param id} and all of its children {@code ProductCategory}.
     * From the resulting {@code ProductCategory}, we validate if
     * all categories have {@code Product attached}.
     *
     * @param categoryId is {@code ProductCategory} to be searched
     * @return {@code 0 or greater than 0} where 0 means all the
     * {@code ProductCategory} have no {@code Product} attached
     * whilst greater than zero means 1 or more {@code Product} is
     * attached to a {@code ProductCategory}
     * */
    public boolean validateProductAttachedToCategoryById(@NotNull long categoryId) {
        String sql = """
        WITH RECURSIVE category (id, parent) AS
        (
            SELECT
                c.category_id,
                c.parent_category_id
            FROM product_category c
            WHERE c.category_id = :id
            UNION ALL
            SELECT
                pc.category_id,
                pc.parent_category_id
            FROM category cat
            INNER JOIN product_category pc
            ON cat.id = pc.parent_category_id
        )
        SELECT COUNT(p.product_id)
        FROM category c1
        INNER JOIN product p
        ON c1.id = p.category_id;
        """;

        Integer i = jdbcClient.sql(sql)
                .param("id", categoryId)
                .query(Integer.class)
                .single();

        return i > 0;
    }

    /**
     * Validates if 1 or more rows depends on {@code ProductCategory} by
     * {@param id} as its parent.
     *
     * @param categoryId is {@code ProductCategory} property {@code categoryId}
     * @return {@code 0 or greater than 0} where 0 means it doesn't have a
     * child {@code ProductCategory} and greater than 0 means 1 or more rows
     * depends on it as a parent.
     * */
    public boolean validateCategoryIsAParent(@NotNull long categoryId) {
        String sql = """
        SELECT COUNT(c.category_id)
        FROM product_category c
        WHERE c.parent_category_id = :id
        """;

        Integer i = jdbcClient.sql(sql)
                .param("id", categoryId)
                .query(Integer.class)
                .single();

        return i > 0;
    }

    public boolean validateCategoryNameIsNotAssignedToCategoryId(
            @NotNull long categoryId,
            @NotNull String categoryName
    ) {
        String sql = """
        SELECT COUNT(c.category_id)
        FROM product_category c
        WHERE c.name = :name AND c.parent_category_id != :id
        """;
        Integer i = jdbcClient.sql(sql)
                .param("name", categoryName)
                .param("id", categoryId)
                .query(Integer.class)
                .single();

        return i > 0;
    }

    @Transactional
    public void updateNameAndVisibilityByCategoryId(
            @NotNull String name,
            @NotNull boolean visible,
            @NotNull long categoryId
    ) {
        String sql = """
        UPDATE product_category c
        SET c.name = :name, c.is_visible = :status
        WHERE c.category_id = :id
        """;
        jdbcClient.sql(sql)
                .param("name", name)
                .param("status", visible ? 1 : 0)
                .param("id", categoryId)
                .update();
    }

    /**
     * Using native sql query, method updates a {@code ProductCategory} parentId
     * based on its categoryId
     * */
    @Transactional
    public void updateCategoryParentId(
            @NotNull long categoryId,
            Long parentId
    ) {
        String sql = """
        UPDATE product_category c
        SET c.parent_category_id = :parent
        WHERE c.category_id = :id
        """;
        jdbcClient.sql(sql)
                .param("parent", parentId)
                .param("id", categoryId)
                .update();
    }

    /**
     * Using native sql query, we get all {@code ProductCategory} that have
     * {@code parent_category_id} equalling {@param categoryId} and then update their
     * visibility to false.
     *
     * @param categoryId is all {@code ProductCategory} who have their
     *                   {@code parent_category_id} equalling.
     * */
    @Transactional
    public void updateAllChildrenToFalseByCategoryId(@NotNull Long categoryId) {
        String sql = """
        WITH RECURSIVE category (id) AS
        (
            SELECT c.category_id FROM product_category AS c WHERE c.parent_category_id = :id
            UNION ALL
            SELECT pc.category_id FROM category cat INNER JOIN product_category pc ON cat.id = pc.parent_category_id
        )
        UPDATE product_category c, (SELECT rec.id FROM category rec) AS rec
        SET c.is_visible = 0
        WHERE c.category_id = rec.id
        """;

        jdbcClient.sql(sql)
                .param("id", categoryId)
                .update();
    }

    /**
     * Using native sql query and Spring Data projection, method returns all
     * children of specified {@code ProductCategory} {@code id}.
     * For more about using common table expression CTE visit
     * <a href="https://dev.mysql.com/doc/refman/8.0/en/with.html#common-table-expressions-recursive">...</a>
     *
     * @param categoryId is categoryId in {@code ProductCategory}
     * @return a list of {@code CategoryPojo} objects
     * */
    public List<CategoryMapper> allChildrenOfParentCategoryByParentId(@NotNull long categoryId) {
        String sql = """
        WITH RECURSIVE category (id, name, status, parent) AS
        (
            SELECT
                c.category_id,
                c.name,
                c.is_visible,
                c.parent_category_id
            FROM product_category c
            WHERE c.parent_category_id = :id
            UNION ALL
            SELECT
                pc.category_id,
                pc.name,
                pc.is_visible,
                pc.parent_category_id
            FROM category cat
            INNER JOIN product_category pc
            ON cat.id = pc.parent_category_id
        )
        SELECT
            c1.id AS id,
            c1.name AS name,
            c1.parent AS parent,
            c1.status AS status
        FROM category c1;
        """;

        return jdbcClient.sql(sql)
                .param("id", categoryId)
                .query(CategoryMapper.class)
                .list();
    }

}
