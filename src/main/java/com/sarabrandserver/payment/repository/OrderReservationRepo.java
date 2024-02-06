package com.sarabrandserver.payment.repository;

import com.sarabrandserver.enumeration.ReservationStatus;
import com.sarabrandserver.payment.entity.OrderReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Contains native query
 * */
@Repository
public interface OrderReservationRepo extends JpaRepository<OrderReservation, Long> {

    /**
     * Updates a {@code ProductSku} by adding to its existing inventory and replaces
     * the qty of a {@code OrderReservation}.
     *
     * @param productSkuQty is the number of type {@code int} to be added to a
     * {@code ProductSku} inventory.
     * @param reservationQty replaces a {@code OrderReservation} qty.
     * @param expire replaces the expire_at property of a {@code ShoppingSession}.
     * @param cookie is a unique string property of {@code ShoppingSession}
     *               that is unique to every device that visits our application. It
     *               is needed to find the {@code OrderReservation} and
     *               {@code ProductSku} associated to the device.
     * @param sku is a unique string for every {@code ProductSku}. It is needed
     *            to find the associated {@code ProductSku} to update
     * @param status is of {@code ReservationStatus} and it always has to be PENDING.
     * */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
    UPDATE product_sku s
    INNER JOIN order_reservation o ON s.sku_id = o.sku_id
    INNER JOIN shopping_session sh ON o.session_id = sh.session_id
    SET
    s.inventory = (s.inventory - :productSkuQty),
    o.qty = :reservationQty,
    o.expire_at = :expire
    WHERE s.sku = :sku AND sh.cookie = :cookie AND o.status = :#{#status.name()}
    """)
    void deductFromProductSkuInventoryAndReplaceReservationQty(
            int productSkuQty,
            int reservationQty,
            Date expire,
            String cookie,
            String sku,
            @Param(value = "status") ReservationStatus status
    );

    /**
     * Updates a {@code ProductSku} by adding to its existing inventory and replaces
     * the qty of a {@code OrderReservation}.
     *
     * @param productSkuQty is the number of type {@code int} to be added to a
     * {@code ProductSku} inventory.
     * @param reservationQty replaces a {@code OrderReservation} qty.
     * @param expire replaces the expire_at property of a {@code ShoppingSession}.
     * @param cookie is a unique string property of {@code ShoppingSession}
     *               that is unique to every device that visits our application. It
     *               is needed to find the {@code OrderReservation} and
     *               {@code ProductSku} associated to the device.
     * @param sku is a unique string for every {@code ProductSku}. It is needed
     *            to find the associated {@code ProductSku} to update
     * @param status is of {@code ReservationStatus} and it always has to be PENDING.
     * */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
    UPDATE product_sku s
    INNER JOIN order_reservation o ON s.sku_id = o.sku_id
    INNER JOIN shopping_session sh ON o.session_id = sh.session_id
    SET
    s.inventory = (s.inventory + :productSkuQty),
    o.qty = :reservationQty,
    o.expire_at = :expire
    WHERE sh.cookie = :cookie AND s.sku = :sku AND o.status = :#{#status.name()}
    """)
    void addToProductSkuInventoryAndReplaceReservationQty(
            int productSkuQty,
            int reservationQty,
            Date expire,
            String cookie,
            String sku,
            @Param(value = "status") ReservationStatus status
    );

    @Query("SELECT o FROM OrderReservation o WHERE o.expireAt <= :date AND o.status = :status")
    List<OrderReservation> allPendingExpiredReservations(Date date, ReservationStatus status);

    @Query("SELECT o FROM OrderReservation o WHERE o.expireAt > :date AND o.status = :status")
    List<OrderReservation> allPendingNoneExpiredReservations(Date date, ReservationStatus status);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM OrderReservation o WHERE o.reservationId = :id")
    void deleteOrderReservationByReservationId(long id);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM OrderReservation o WHERE o.expireAt <= :date")
    void deleteExpired(Date date);

}