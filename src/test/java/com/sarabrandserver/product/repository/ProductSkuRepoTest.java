package com.sarabrandserver.product.repository;

import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.RepositoryTestData;
import com.sarabrandserver.enumeration.PaymentStatus;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.payment.entity.OrderDetail;
import com.sarabrandserver.payment.entity.OrderReservation;
import com.sarabrandserver.payment.entity.PaymentDetail;
import com.sarabrandserver.payment.repository.OrderDetailRepository;
import com.sarabrandserver.payment.repository.OrderReservationRepo;
import com.sarabrandserver.payment.repository.PaymentDetailRepo;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.util.CustomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static com.sarabrandserver.enumeration.ReservationStatus.PENDING;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ProductSkuRepoTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductDetailRepo detailRepo;
    @Autowired
    private PriceCurrencyRepo priceCurrencyRepo;
    @Autowired
    private ProductImageRepo imageRepo;
    @Autowired
    private ProductSkuRepo skuRepo;
    @Autowired
    private OrderDetailRepository orderRepository;
    @Autowired
    private PaymentDetailRepo paymentDetailRepo;
    @Autowired
    private OrderReservationRepo reservationRepo;
    @Autowired
    private ShoppingSessionRepo sessionRepo;
    @Autowired
    private CartItemRepo cartItemRepo;

    @Test
    void itemBeenBought() {
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

        var paymentDetail = paymentDetailRepo
                .save(
                        PaymentDetail.builder()
                                .name("James Frank")
                                .email("james@email.com")
                                .phone("0000000000")
                                .referenceId("unique-payment-categoryId")
                                .paymentProvider("PayStack")
                                .currency(SarreCurrency.NGN)
                                .amount(new BigDecimal("25750"))
                                .paymentStatus(PaymentStatus.CONFIRMED)
                                .createAt(new Date())
                                .address(null)
                                .orderDetails(new HashSet<>())
                                .build()
                );

        // then
        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        assertEquals(0, skuRepo.skuHasBeenPurchased(sku.getSku()));
        orderRepository
                .save(new OrderDetail(sku.getInventory(), sku, paymentDetail));

        assertEquals(1, skuRepo.skuHasBeenPurchased(sku.getSku()));
    }

    @Test
    void updateInventoryOnMakingReservation() {
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build()
                );

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        assertNotEquals(0, sku.getInventory());

        skuRepo.updateProductSkuInventoryBySubtractingFromExistingInventory(sku.getSku(), sku.getInventory());

        var optional = skuRepo.findBySku(sku.getSku());
        assertFalse(optional.isEmpty());
        assertEquals(0, optional.get().getInventory());
    }

    @Test
    void updateInventory() {
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        assertNotEquals(0, sku.getInventory());

        skuRepo.updateProductSkuInventoryByAddingToExistingInventory(sku.getSku(), sku.getInventory());

        var optional = skuRepo.findBySku(sku.getSku());
        assertFalse(optional.isEmpty());
        assertTrue(optional.get().getInventory() > sku.getInventory());
    }

    @Test
    void validateOnDeleteNoActionConstraintForProductSku() {
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        var paymentDetail = paymentDetailRepo
                .save(
                        PaymentDetail.builder()
                                .name("James Frank")
                                .email("james@email.com")
                                .phone("0000000000")
                                .referenceId("unique-payment-categoryId")
                                .paymentProvider("PayStack")
                                .currency(SarreCurrency.NGN)
                                .amount(new BigDecimal("25750"))
                                .paymentStatus(PaymentStatus.CONFIRMED)
                                .createAt(new Date())
                                .address(null)
                                .orderDetails(new HashSet<>())
                                .build()
                );

        // then
        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        ProductSku sku = skus.getFirst();

        // save OrderDetail
        orderRepository.save(new OrderDetail(1, sku, paymentDetail));

        var session = this.sessionRepo
                .save(
                        new ShoppingSession(
                                "cookie",
                                new Date(),
                                CustomUtil.toUTC(new Date(Instant.now().plus(1, HOURS).toEpochMilli())),
                                new HashSet<>(),
                                new HashSet<>()
                        )
                );

        // save OrderReservation
        Date current = new Date();
        reservationRepo
                .save(
                        new OrderReservation(
                                UUID.randomUUID().toString(),
                                sku.getInventory() - 1,
                                PENDING,
                                CustomUtil.toUTC(
                                        new Date(current
                                                .toInstant()
                                                .minus(5, HOURS)
                                                .toEpochMilli()
                                        )
                                ),
                                sku,
                                session
                        )
                );

        // save CartItem
        cartItemRepo.save(new CartItem(Integer.MAX_VALUE, session, sku));

        assertThrows(DataIntegrityViolationException.class,
                () -> skuRepo.deleteProductSkuBySku(sku.getSku()));
    }

    @Test
    void validateConstraintProductSkuInvCannotBeLessThanZero() {
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
        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());

        assertThrows(JpaSystemException.class,
                () -> skuRepo.updateProductSkuInventoryByAddingToExistingInventory(
                        skus.getFirst().getSku(),
                        -100
                )
        );
    }

}