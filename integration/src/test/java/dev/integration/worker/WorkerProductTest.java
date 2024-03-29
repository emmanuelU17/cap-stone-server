package dev.integration.worker;

import com.github.javafaker.Faker;
import dev.integration.MainTest;
import dev.integration.TestData;
import dev.webserver.category.response.WorkerCategoryResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class WorkerProductTest extends MainTest {

    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    static void before() {
        assertNotNull(COOKIE);

        headers.set("Cookie", "JSESSIONID=" + COOKIE.getValue());
    }

    @Test
    void shouldSuccessfullyRetrieveProducts() {
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        var get = testTemplate.exchange(
                PATH + "/api/v1/worker/product",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                WorkerCategoryResponse.class
        );

        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

    @Test
    void shouldSuccessfullyCreateAProduct() throws IOException {
        headers.set("content-type", MediaType.MULTIPART_FORM_DATA_VALUE);

        var productDto = TestData
                .createProductDTO(
                        new Faker().commerce().productName(),
                        1,
                        TestData.sizeInventoryDTOArray(3)
                );

        // create the json
        String dto = mapper.writeValueAsString(productDto);

        MultiValueMap<String, Object> multipartData = TestData.files(dto);

        // request
        var post = testTemplate.postForEntity(
                PATH + "/api/v1/worker/product",
                new HttpEntity<>(multipartData, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(201), post.getStatusCode());
    }

    @Test
    void shouldSuccessfullyUpdateAProduct() {
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        var dto = TestData
                .updateProductDTO(
                        "product-uuid",
                        "new-product-name",
                        1
                );

        var update = testTemplate.exchange(
                PATH + "/api/v1/worker/product",
                HttpMethod.PUT,
                new HttpEntity<>(dto, headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), update.getStatusCode());
    }

    @Test
    void shouldSuccessfullyDeleteAProduct() {
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        var delete = testTemplate.exchange(
                PATH + "/api/v1/worker/product?id=product-uuid-2",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(204), delete.getStatusCode());
    }

    @Test
    void shouldNotSuccessfullyDeleteAProduct() {
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        var delete = testTemplate.exchange(
                PATH + "/api/v1/worker/product?id=product-uuid",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatusCode.valueOf(409), delete.getStatusCode());
    }

}
