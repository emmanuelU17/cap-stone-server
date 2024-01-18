package com.sarabrandserver.category.repository;

import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.projection.CategoryPojo;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.projection.ProductPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * contains native query
 * */
@Repository
public interface CategoryRepository extends JpaRepository<ProductCategory, Long> {

    @Query("SELECT c FROM ProductCategory c WHERE c.name = :name")
    Optional<ProductCategory> findByName(@Param(value = "name") String name);

    /**
     * Using native sql query, method retrieves {@code ProductCategory}
     * by {@param id} and all of its children {@code ProductCategory}.
     * From the resulting {@code ProductCategory}, we validate if
     * all categories have {@code Product attached}.
     *
     * @param id is {@code ProductCategory} to be searched
     * @return {@code 0 or greater than 0} where 0 means all the
     * {@code ProductCategory} have no {@code Product} attached
     * whilst greater than zero means 1 or more {@code Product} is
     * attached to a {@code ProductCategory}
     * */
    @Query(nativeQuery = true, value = """
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
    """)
    int validateProductAttached(long id);

    /**
     * Validates if 1 or more rows depends on {@code ProductCategory} by
     * {@param id} as its parent.
     *
     * @param id is {@code ProductCategory} property {@code categoryId}
     * @return {@code 0 or greater than 0} where 0 means it doesn't have a
     * child {@code ProductCategory} and greater than 0 means 1 or more rows
     * depends on it as a parent.
     * */
    @Query(value = """
    SELECT COUNT (c.categoryId)
    FROM ProductCategory c
    WHERE c.parentCategory.categoryId = :id
    """)
    int validate_category_is_a_parent(long id);

    @Query(value = """
    SELECT
    COUNT(pc.categoryId)
    FROM ProductCategory pc
    WHERE pc.name = :name AND pc.categoryId != :id
    """)
    int onDuplicateCategoryName(long id, String name);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("""
    UPDATE ProductCategory pc
    SET  pc.name = :name, pc.isVisible = :visible
    WHERE pc.categoryId = :id
    """)
    void update(
            @Param(value = "name") String name,
            @Param(value = "visible") boolean visible,
            @Param(value = "id") long id
    );

    /**
     * Using native sql query, method updates a {@code ProductCategory} parentId
     * based on its categoryId
     * */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(nativeQuery = true, value= """
    UPDATE product_category c
    SET c.parent_category_id = :parentId
    WHERE c.category_id = :categoryId
    """)
    void update_category_parentId_based_on_categoryId(long categoryId, long parentId);

    /**
     * Using native sql query, we get all {@code ProductCategory} that have
     * {@code parent_category_id} equalling {@param categoryId} and then update their
     * visibility to false.
     *
     * @param categoryId is all {@code ProductCategory} who have their
     *                   {@code parent_category_id} equalling.
     * */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(nativeQuery = true, value = """
    WITH RECURSIVE category (id) AS
    (
        SELECT c.category_id FROM product_category AS c WHERE c.parent_category_id = :categoryId
        UNION ALL
        SELECT pc.category_id FROM category cat INNER JOIN product_category pc ON cat.id = pc.parent_category_id
    )
    UPDATE product_category c, (SELECT rec.id FROM category rec) AS rec
    SET c.is_visible = 0
    WHERE c.category_id = rec.id
    """)
    void updateAllChildrenToFalse(long categoryId);

    @Query(value = """
    SELECT
    p.uuid as uuid,
    p.name as name,
    (SELECT c.currency FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS currency,
    (SELECT c.price FROM PriceCurrency c WHERE p.productId = c.product.productId AND c.currency = :currency) AS price,
    p.defaultKey as image
    FROM Product p
    INNER JOIN ProductCategory c ON p.productCategory.categoryId = c.categoryId
    WHERE c.categoryId = :id
    """)
    Page<ProductPojo> allProductsByCategoryWorker(SarreCurrency currency, long id, Pageable page);

    /**
     * Using native sql query, method returns all {@code Product} based on
     * {@code ProductCategory} and its children.
     *
     * @param categoryId is the {@code ProductCategory} and all of its children
     * @param currency is the string value of {@code SarreCurrency}
     * @param page is the of {@code org.springframework.data.domain.Pageable}
     * @return a {@code org.springframework.data.domain.Page} of {@code ProductPojo}
     * */
    @Query(nativeQuery = true, value = """
    WITH RECURSIVE category (id) AS
    (
        SELECT c.category_id FROM product_category AS c WHERE c.category_id = :categoryId
        UNION ALL
        SELECT pc.category_id FROM category cat INNER JOIN product_category pc ON cat.id = pc.parent_category_id
    )
    SELECT
    p.uuid AS uuid,
    p.name AS name,
    p.description AS description,
    c.currency AS currency,
    c.price AS price,
    p.default_image_key AS image
    FROM category c1
    INNER JOIN product p ON c1.id = p.category_id
    INNER JOIN price_currency c ON p.product_id = c.product_id
    INNER JOIN product_detail d ON p.product_id = d.product_id
    INNER JOIN product_sku s ON d.detail_id = s.detail_id
    WHERE d.is_visible = 1 AND s.inventory > 0 AND c.currency = :#{#currency.name()}
    GROUP BY c.currency, c.price, p.uuid, p.name, p.description, p.default_image_key;
    """)
    Page<ProductPojo> allProductsByCategoryIdWhereProductIsVisibleAndInStock(
            long categoryId,
            SarreCurrency currency,
            Pageable page
    );

    /**
     * Using native sql query and Spring Data projection, method returns all
     * children of specified {@code ProductCategory} {@code id}.
     * For more about using common table expression CTE visit
     * <a href="https://dev.mysql.com/doc/refman/8.0/en/with.html#common-table-expressions-recursive">...</a>
     *
     * @param id is categoryId in {@code ProductCategory}
     * @return a list of {@code CategoryPojo} objects
     * */
    @Query(nativeQuery = true, value = """
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
    """)
    List<CategoryPojo> all_categories_by_categoryId(long id);

    @Query(value = """
    SELECT
    c.categoryId AS id,
    c.name AS name,
    c.isVisible AS status,
    c.parentCategory.categoryId AS parent
    FROM ProductCategory c
    """)
    List<CategoryPojo> allCategories();

}
