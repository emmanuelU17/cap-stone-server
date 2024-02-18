package com.sarabrandserver.checkout;

import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.payment.projection.TotalPojo;
import com.sarabrandserver.shipping.entity.ShipSetting;
import com.sarabrandserver.shipping.service.ShippingService;
import com.sarabrandserver.tax.Tax;
import com.sarabrandserver.tax.TaxService;
import com.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    @Setter
    @Value("${cart.cookie.name}")
    private String CART_COOKIE;
    @Setter
    @Value(value = "${cart.split}")
    private String SPLIT;

    private final ShippingService shippingService;
    private final TaxService service;
    private final ShoppingSessionRepo shoppingSessionRepo;
    private final CartItemRepo cartItemRepo;

    /**
     * Generates checkout information based on a user's country and selected currency.
     * <p>
     * This method processes the user's checkout request by retrieving necessary information
     * such as the {@link ShoppingSession}, {@link CartItem}, {@link ShipSetting},
     * and {@link Tax}. It then calculates the total amount for the checkout and constructs
     * a {@link Checkout} object containing the shipping price, tax information, and total
     * amount in the users choice of currency.
     *
     * @param req      The {@link HttpServletRequest} representing the user's request.
     * @param country  The country entered by the user during checkout.
     * @param currency The currency selected by the user for checkout.
     * @return A {@link Checkout} object containing shipping price, tax details, and total amount.
     * @throws CustomNotFoundException If any required information is missing or invalid.
     */
    public Checkout checkout(HttpServletRequest req, String country, SarreCurrency currency) {
        CustomObject obj = createCustomObjectForShoppingSession(req, country);

        List<TotalPojo> list = this.cartItemRepo
                .totalPojoByShoppingSessionId(obj.session().shoppingSessionId(), currency);

        BigDecimal ship = currency.equals(SarreCurrency.USD)
                ? obj.ship().usdPrice()
                : obj.ship().ngnPrice();

        BigDecimal total = CustomUtil
                .calculateTotal(
                        CustomUtil.cartItemsTotal(list),
                        obj.tax().rate(),
                        ship
                );

        // TODO add tax difference

        return new Checkout(ship, obj.tax().name(), obj.tax().rate(), new BigDecimal(""), total);
    }

    /**
     * Creates a {@link CustomObject} based on the provided {@link HttpServletRequest} and
     * country.
     * <p>
     * This method retrieves custom cookie from the HttpServletRequest to find associated
     * {@link ShoppingSession} for the device. It then retrieves the associated cart items
     * and checks if the cart is empty. Next, it retrieves the shipping information based
     * on the provided country. Finally, it retrieves the tax information. Using this
     * information, it constructs and returns a {@link CustomObject} containing the
     * {@link ShoppingSession}, {@link CartItem}, {@link ShipSetting}, and {@link Tax}.
     *
     * @param req     The HttpServletRequest containing the {@link ShoppingSession} cookie.
     * @param country The country for which {@link ShipSetting} information is retrieved.
     * @return A {@link CustomObject} containing the shopping session, cart items, shipping,
     * and tax.
     * @throws CustomNotFoundException If custom cookie does not contain in
     *                                 {@link HttpServletRequest}, the shopping session is invalid,
     *                                 or the cart is empty.
     */
    public CustomObject createCustomObjectForShoppingSession(HttpServletRequest req, String country) {
        Cookie cookie = CustomUtil.cookie(req, CART_COOKIE);

        if (cookie == null) {
            throw new CustomNotFoundException("no cookie found. kindly refresh window");
        }

        var optional = shoppingSessionRepo
                .shoppingSessionByCookie(cookie.getValue().split(SPLIT)[0]);

        if (optional.isEmpty()) {
            throw new CustomNotFoundException("invalid shopping session");
        }

        ShoppingSession session = optional.get();

        var carts = cartItemRepo
                .cartItemsByShoppingSessionId(session.shoppingSessionId());

        if (carts.isEmpty()) {
            throw new CustomNotFoundException("cart is empty");
        }

        ShipSetting ship = shippingService
                .shippingByCountryElseReturnDefault(country);

        Tax tax = service.taxById(1);

        return new CustomObject(session, carts, ship, tax);
    }

}
