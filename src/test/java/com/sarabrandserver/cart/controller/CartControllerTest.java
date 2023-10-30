package com.sarabrandserver.cart.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.cart.service.CartService;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.user.repository.UserRepository;
import com.sarabrandserver.user.repository.UserRoleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerTest extends AbstractIntegrationTest {

    private final String path = "/api/v1/client/cart";

    @Autowired private ShoppingSessionRepo shoppingSessionRepo;
    @Autowired private CartItemRepo cartItemRepo;
    @Autowired private CartService cartService;
    @Autowired private AuthService authService;
    @Autowired private UserRoleRepository roleRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        var registerDTO = new RegisterDTO(
                "SEJU",
                "Development",
                "fart@client.com",
                "",
                "000-000-0000",
                "password"
        );
        this.authService.workerRegister(registerDTO);

        var sku = productSku();
        var dto = new CartDTO(sku.getSku(), sku.getInventory());
//        this.cartService.create(dto);
    }

    @AfterEach
    void tearDown() {
        this.shoppingSessionRepo.deleteAll();
        this.roleRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    private ProductSku productSku() {
        var list = this.productSkuRepo.findAll();
        assertFalse(list.isEmpty());
        return list.get(0);
    }

    @Test
    @WithMockUser(username = "fart@client.com", password = "password", roles = {"CLIENT"})
    void listCartItems() throws Exception {
        this.MOCKMVC
                .perform(get(path).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void listCartItemsUSD() throws Exception {
        this.MOCKMVC
                .perform(get(path).param("currency", "usd").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "fart@client.com", password = "password", roles = {"CLIENT"})
    void create_new_shopping_session() throws Exception {
        var sku = productSku();

        var dto = new CartDTO(sku.getSku(), sku.getInventory());

        this.MOCKMVC
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "fart@client.com", password = "password", roles = {"CLIENT"})
    void add_to_existing_shopping_session() throws Exception {
        var sku = productSku();
        var dto = new CartDTO(sku.getSku(), sku.getInventory());

        this.MOCKMVC
                .perform(post(path)
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .with(csrf())
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "fart@client.com", password = "password", roles = {"CLIENT"})
    void delete_item() throws Exception {
        var list = this.cartItemRepo.findAll();
        assertFalse(list.isEmpty());
        var cart = list.get(0);

        this.MOCKMVC
                .perform(delete(path)
                        .param("sku", cart.getSku())
                        .with(csrf())
                )
                .andExpect(status().isOk());

        var find = this.cartItemRepo.findById(cart.getCartId());
        assertThrows(NoSuchElementException.class, find::get);
    }
}