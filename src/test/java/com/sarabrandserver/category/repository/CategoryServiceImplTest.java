package com.sarabrandserver.category.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.repository.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class CategoryServiceImplTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryServiceImpl categoryService;
    @Autowired
    private ProductServiceImpl productService;

    @Test
    void allCategories() {
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

        var collection = categoryService
                .save(
                        ProductCategory
                                .builder()
                                .name("collection")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        categoryService
                .save(
                        ProductCategory
                                .builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var list = categoryService.allCategories();
        assertEquals(3, list.size());

        categoryService
                .save(
                        ProductCategory
                                .builder()
                                .name("summer")
                                .isVisible(true)
                                .parentCategory(collection)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var list1 = categoryService.allCategories();
        assertEquals(4, list1.size());
    }

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

            assertTrue(category.isVisible());
            assertNotNull(category.getCategoryId());
        });
    }

    @Test
    void save_category_where_parent_is_not_null() {
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

            assertTrue(category.isVisible());
            assertNotNull(category.getCategoryId());

            categoryService
                    .save(
                            ProductCategory
                                    .builder()
                                    .name("clothes")
                                    .isVisible(true)
                                    .parentCategory(category)
                                    .categories(new HashSet<>())
                                    .product(new HashSet<>())
                                    .build()
                    );
        });
    }

    @Test
    void assert_duplicate_exception_thrown_for_category_name() {
        assertThrows(DuplicateKeyException.class, () -> {
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

            assertTrue(category.isVisible());
            assertNotNull(category.getCategoryId());

            categoryService
                    .save(
                            ProductCategory
                                    .builder()
                                    .name("category")
                                    .isVisible(true)
                                    .categories(new HashSet<>())
                                    .product(new HashSet<>())
                                    .build()
                    );
        });
    }

    @Test
    void validateProductAttachedToCategoryById() {
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

        assertFalse(categoryService.validateProductAttachedToCategoryById(category.getCategoryId()));

        productService
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

        assertTrue(categoryService.validateProductAttachedToCategoryById(category.getCategoryId()));
    }

    @Test
    void validateCategoryIsAParent() {
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

        assertFalse(categoryService.validateCategoryIsAParent(category.getCategoryId()));

        var clothes = categoryService
                .save(
                        ProductCategory
                                .builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        assertTrue(categoryService.validateCategoryIsAParent(category.getCategoryId()));
        assertFalse(categoryService.validateCategoryIsAParent(clothes.getCategoryId()));
    }

    // TODO
    @Test
    void validateCategoryNameIsNotAssignedToCategoryId() {
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

        boolean bool = categoryService
                .validateCategoryNameIsNotAssignedToCategoryId(
                        category.getCategoryId(),
                        "top"
                );

        assertFalse(bool);

        categoryService
                .save(
                        ProductCategory
                                .builder()
                                .name("top")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        bool = categoryService
                .validateCategoryNameIsNotAssignedToCategoryId(
                        category.getCategoryId(),
                        "top"
                );

        assertTrue(bool);
    }

    @Test
    void updateNameAndVisibilityByCategoryId() {
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

        categoryService
                .updateNameAndVisibilityByCategoryId(
                        "collection",
                        false,
                        category.getCategoryId()
                );

        var optional = categoryService.findById(category.getCategoryId());

        assertFalse(optional.isEmpty());
        assertEquals("collection", optional.get().getName());
        assertFalse(optional.get().isVisible());
    }

    @Test
    void updateCategoryParentId() {
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

        var furniture = categoryService
                .save(
                        ProductCategory
                                .builder()
                                .name("furniture")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        categoryService
                .updateCategoryParentId(furniture.getCategoryId(), null);

        assertFalse(categoryService
                .validateCategoryIsAParent(category.getCategoryId())
        );

        assertFalse(categoryService
                .validateCategoryIsAParent(furniture.getCategoryId())
        );

        categoryService
                .save(
                        ProductCategory
                                .builder()
                                .name("chair")
                                .isVisible(true)
                                .parentCategory(furniture)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        assertTrue(categoryService
                .validateCategoryIsAParent(furniture.getCategoryId())
        );
    }

    @Test
    void updateAllChildrenToFalseByCategoryId() {
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

        var clothes = categoryService
                .save(
                        ProductCategory
                                .builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var top = categoryService
                .save(
                        ProductCategory
                                .builder()
                                .name("top")
                                .isVisible(true)
                                .parentCategory(clothes)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        categoryService.updateAllChildrenToFalseByCategoryId(category.getCategoryId());

        var optionalCat = categoryService.findById(category.getCategoryId());
        assertFalse(optionalCat.isEmpty());
        assertTrue(optionalCat.get().isVisible());

        var optionalClothes = categoryService.findById(clothes.getCategoryId());
        assertFalse(optionalClothes.isEmpty());
        assertFalse(optionalClothes.get().isVisible());

        var optionalTop = categoryService.findById(top.getCategoryId());
        assertFalse(optionalTop.isEmpty());
        assertFalse(optionalTop.get().isVisible());
    }

    @Test
    void allChildrenOfParentCategoryByParentId() {
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

        var clothes = categoryService
                .save(
                        ProductCategory
                                .builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var top = categoryService
                .save(
                        ProductCategory
                                .builder()
                                .name("top")
                                .isVisible(true)
                                .parentCategory(clothes)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        var list = categoryService
                .allChildrenOfParentCategoryByParentId(category.getCategoryId());

        assertEquals(2, list.size());
        assertEquals(1, categoryService
                .allChildrenOfParentCategoryByParentId(clothes.getCategoryId())
                .size()
        );

        assertEquals(0, categoryService
                .allChildrenOfParentCategoryByParentId(top.getCategoryId())
                .size()
        );
    }
    
    // TODO allProductsByCategoryWorker and productsByCategoryId

}