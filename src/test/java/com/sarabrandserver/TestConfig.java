package com.sarabrandserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

// https://spring.io/blog/2023/06/23/improved-testcontainers-support-in-spring-boot-3-1
@TestConfiguration(proxyBeanMethods = false)
class TestConfig {

    static final Logger log = LoggerFactory.getLogger(TestConfig.class);

    @Bean
    @ServiceConnection
    @RestartScope
    static MySQLContainer<?> mySQLContainer() {
        try (var sql = new MySQLContainer<>("mysql:8.0")) {
            return sql.withDatabaseName("sara_brand_db")
                    .withUsername("sara")
                    .withPassword("sara");
        } catch (RuntimeException ex) {
            log.error("failed to start up MySQL in test/dev mode");
            throw new RuntimeException();
        }
    }

}
