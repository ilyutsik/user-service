package org.innowise.userservice.config;


import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainersConfig {

    public static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:14")
                    .withDatabaseName("testDB")
                    .withUsername("123")
                    .withPassword("123");

    public static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7")
                    .withExposedPorts(6379);

    static {
        POSTGRES.start();
        REDIS.start();
    }

    private TestContainersConfig() {
    }
}

