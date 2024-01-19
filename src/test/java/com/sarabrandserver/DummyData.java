package com.sarabrandserver;

import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
class DummyData {

    @Bean
    public CommandLineRunner runner(
            AuthService authService,
            UserRepository repository,
            WorkerCategoryService catService,
            WorkerProductService workerProductService
    ) {
        return args -> {
            extracted(catService, workerProductService);

            if (repository.findByPrincipal("admin@admin.com").isEmpty()) {
                var dto = new RegisterDTO(
                        "SEJU",
                        "Development",
                        "admin@admin.com",
                        "",
                        "0000000000",
                        "password123"
                );
                authService.workerRegister(dto);
            }

        };
    }

    private static void extracted(WorkerCategoryService catService, WorkerProductService service) {
        var category = ProductCategory.builder()
                .categoryId(1L)
                .build();
        catService.create(new CategoryDTO("category", true, null));
        TestData.dummyProducts(category, 2, service);

        var clothes = ProductCategory.builder()
                .categoryId(2L)
                .build();
        catService.create(new CategoryDTO("clothes", true, 1L));
        TestData.dummyProducts(clothes, 5, service);

        var shirt = ProductCategory.builder()
                .categoryId(3L)
                .build();
        catService.create(new CategoryDTO("t-shirt", true, 2L));
        TestData.dummyProducts(shirt, 10, service);

        var furniture = ProductCategory.builder()
                .categoryId(4L)
                .build();
        catService.create(new CategoryDTO("furniture", true, null));
        TestData.dummyProducts(furniture, 3, service);

        var collection = ProductCategory.builder()
                .categoryId(5L)
                .build();
        catService.create(new CategoryDTO("collection", true, null));
        TestData.dummyProducts(collection, 1, service);

        var winter = ProductCategory.builder()
                .categoryId(6L)
                .build();
        catService.create(new CategoryDTO("winter 2024", true, 5L));
        TestData.dummyProducts(winter, 15, service);
    }

}
