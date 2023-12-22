package com.sarabrandserver.order.repository;

import com.sarabrandserver.enumeration.ReservationStatus;
import com.sarabrandserver.order.entity.OrderReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Contains native query
 * */
@Repository
public interface OrderReservationRepo extends JpaRepository<OrderReservation, Long> {

    @Query("SELECT o FROM OrderReservation o WHERE o.cookie = :cookie AND o.status = :status")
    List<OrderReservation> orderReservationByCookie(String cookie, ReservationStatus status);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    DELETE FROM OrderReservation o
    WHERE o.cookie = :cookie AND o.sku = :sku AND o.status = :status
    """)
    void deleteBySku(String cookie, String sku, ReservationStatus status);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
    UPDATE product_sku s
    INNER JOIN order_reservation o ON s.sku = o.sku
    SET
    s.inventory = (s.inventory - :productSkuQty),
    o.qty = :reservationQty,
    o.expire_at = :expire
    WHERE s.sku = :sku AND o.cookie = :cookie AND o.status = :status
    """)
    void onSub(
            int productSkuQty,
            int reservationQty,
            Date expire,
            String cookie,
            String sku,
            ReservationStatus status
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
    UPDATE product_sku s
    INNER JOIN order_reservation o ON s.sku = o.sku
    SET
    s.inventory = (s.inventory + :productSkuQty),
    o.qty = :reservationQty,
    o.expire_at = :expire
    WHERE s.sku = :sku AND o.cookie = :cookie AND o.status = :status
    """)
    void onAdd(
            int productSkuQty,
            int reservationQty,
            Date expire,
            String cookie,
            String sku,
            ReservationStatus status
    );

}