package org.innowise.userservice;

import org.innowise.userservice.config.TestContainersConfig;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class IntegrationTestBase {

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TestContainersConfig.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", TestContainersConfig.POSTGRES::getUsername);
        registry.add("spring.datasource.password",TestContainersConfig.POSTGRES::getPassword);

        registry.add("spring.data.redis.host", TestContainersConfig.REDIS::getHost);
        registry.add("spring.data.redis.port", () -> TestContainersConfig.REDIS.getMappedPort(6379));
    }

}
