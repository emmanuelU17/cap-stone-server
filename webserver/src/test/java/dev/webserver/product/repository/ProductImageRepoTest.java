package dev.webserver.product.repository;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.category.entity.ProductCategory;
import dev.webserver.category.repository.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ProductImageRepoTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductSkuRepo skuRepo;
    @Autowired
    private ProductImageRepo imageRepo;
    @Autowired
    private ProductDetailRepo detailRepo;
    @Autowired
    private PriceCurrencyRepo priceCurrencyRepo;

    @Test
    void imagesByProductDetailId() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());
        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        // when
        var details = detailRepo.findAll();
        assertFalse(details.isEmpty());

        // then
        var images = imageRepo
                .imagesByProductDetailId(details.getFirst().getProductDetailId());

        assertFalse(images.isEmpty());
    }

}