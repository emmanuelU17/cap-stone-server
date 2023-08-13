package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.category.dto.CategoryDTO;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.util.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.util.DetailDTO;
import com.emmanuel.sarabrandserver.product.util.ProductDTO;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class WorkerProductControllerTest {

    private final static String requestMapping = "/api/v1/worker/product";
    private String category = "";
    private String productName = "";

    @Autowired private MockMvc MOCK_MVC;
    @Autowired private WorkerProductService workerService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WorkerCategoryService workerCategoryService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductDetailRepo productDetailRepo;

    @Container private static final MySQLContainer<?> container;

    static {
        container = new MySQLContainer<>("mysql:latest")
                .withDatabaseName("sara_brand_db")
                .withUsername("sara")
                .withPassword("sara");
    }

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @BeforeEach
    void setUp() {
        Set<String> set = new HashSet<>();

        for (int i = 0; i < 20; i++) {
            set.add(new Faker().commerce().department());
        }

        for (String str : set) {
            if (this.category.isEmpty()) {
                this.category = str;
            }
            this.workerCategoryService.create(new CategoryDTO(str, true, ""));
        }

        for (String str : set) {
            var dto = CreateProductDTO.builder()
                    .category(str)
                    .collection("")
                    .name(new Faker().commerce().productName())
                    .desc(new Faker().lorem().characters(255))
                    .price(new BigDecimal(new Faker().commerce().price()))
                    .currency("USD")
                    .visible(true)
                    .qty(new Faker().number().numberBetween(10, 30))
                    .size(new Faker().commerce().material())
                    .colour(new Faker().commerce().color())
                    .build();

            if (this.productName.isEmpty()) {
                this.productName = dto.getName();
            }

            MockMultipartFile[] files = {
                    new MockMultipartFile(
                            "file",
                            "uploads/image1.jpeg",
                            "image/jpeg",
                            "Test image content".getBytes()
                    ),
                    new MockMultipartFile(
                            "file",
                            "uploads/image2.jpeg",
                            "image/jpeg",
                            "Test image content".getBytes()
                    ),
                    new MockMultipartFile(
                            "file",
                            "uploads/image3.jpeg",
                            "image/jpeg",
                            "Test image content".getBytes()
                    ),
            };
            this.workerService.create(dto, files);
        }

        for (int i = 0; i < 50; i++) {
            // Validate deleting
            var dto1 = CreateProductDTO.builder()
                    .category(category)
                    .collection("")
                    .name("custom-product")
                    .desc(new Faker().lorem().characters(255))
                    .price(new BigDecimal(new Faker().commerce().price()))
                    .currency("USD")
                    .visible(true)
                    .qty(new Faker().number().numberBetween(10, 30))
                    .size(new Faker().commerce().material())
                    .colour(new Faker().commerce().color())
                    .build();

            MockMultipartFile[] files1 = {
                    new MockMultipartFile(
                            "file",
                            "uploads/image1.jpeg",
                            "image/jpeg",
                            "Test image content".getBytes()
                    ),
                    new MockMultipartFile(
                            "file",
                            "uploads/image2.jpeg",
                            "image/jpeg",
                            "Test image content".getBytes()
                    ),
                    new MockMultipartFile(
                            "file",
                            "uploads/image3.jpeg",
                            "image/jpeg",
                            "Test image content".getBytes()
                    ),
            };
            this.workerService.create(dto1, files1);
        }
    }

    @AfterEach
    void tearDown() {
        this.productRepository.deleteAll();
        this.categoryRepository.deleteAll();
    }

    /** Testing fetchAll method that returns a ProductResponse. */
    @Test @WithMockUser(username = "admin@admin.com", password = "password", roles = { "WORKER" })
    void fetchAll() throws Exception {
        // Then
        this.MOCK_MVC
                .perform(get(requestMapping)
                        .param("page", "0")
                        .param("size", "50")
                )
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /** Testing fetchAll method that returns a DetailResponse. */
    @Test @WithMockUser(username = "admin@admin.com", password = "password", roles = { "WORKER" })
    void fetchAllDetail() throws Exception {
        this.MOCK_MVC
                .perform(get(requestMapping + "/{name}", "custom-product")
                        .param("page", "0")
                        .param("size", "50")
                )
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(30)));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = { "WORKER" })
    @DisplayName(value = "update all updatable variable")
    void updateProduct() throws Exception {
        // Given
        String id = this.productRepository.findAll().get(0).getUuid();
        var dto = ProductDTO.builder()
                .uuid(id)
                .name("SEJU Development")
                .desc(new Faker().lorem().characters(5, 200))
                .price(new BigDecimal(new Faker().commerce().price()))
                .build();

        // Then
        this.MOCK_MVC
                .perform(put(requestMapping)
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = { "WORKER" })
    @DisplayName(value = "update all updatable variable apart from product name")
    void update() throws Exception {
        // Given
        var product = this.productRepository.findAll().get(1);
        var dto = ProductDTO.builder()
                .uuid(product.getUuid())
                .name(product.getName())
                .desc(new Faker().lorem().characters(5, 200))
                .price(new BigDecimal(new Faker().commerce().price()))
                .build();

        // Then
        this.MOCK_MVC
                .perform(put(requestMapping)
                        .contentType(APPLICATION_JSON)
                        .content(dto.toJson().toString())
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = { "WORKER" })
    @DisplayName(value = "update ProductDetail")
    void updateDetail() throws Exception {
        var detail = this.productDetailRepo.findAll().get(0);
        String dto = new DetailDTO(detail.getSku(), true, 50, "large").toJson().toString();

        // Then
        this.MOCK_MVC
                .perform(put(requestMapping + "/detail")
                        .contentType(APPLICATION_JSON)
                        .content(dto)
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test @WithMockUser(username = "admin@admin.com", password = "password", roles = { "WORKER" })
    void deleteProduct() throws Exception {
        this.MOCK_MVC.perform(delete(requestMapping + "/{name}", this.productName).with(csrf()))
                .andExpect(status().isNoContent());
    }

}