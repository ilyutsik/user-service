package org.innowise.userservice.controller;

import org.innowise.userservice.config.JacksonConfig;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@EnableCaching
@ActiveProfiles("test")
@Import(JacksonConfig.class)
public class IntegrationTestBase {

    @BeforeAll
    static void beforeAll() {
        System.out.println("JDBC URL = " + postgres.getJdbcUrl());
        System.out.println("POSTGRES HOST = " + postgres.getHost());
        System.out.println("POSTGRES PORT = " + postgres.getFirstMappedPort());
    }


    @Container
    protected static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:14")
                    .withDatabaseName("testDB")
                    .withUsername("123")
                    .withPassword("123")
                    .waitingFor(Wait.forListeningPort());;

    @Container
    protected static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7")
                    .withExposedPorts(6379)
                    .waitingFor(Wait.forListeningPort());
    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

}
