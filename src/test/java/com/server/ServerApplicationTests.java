package com.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("ci")
class ServerApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("Active profile: " + System.getProperty("spring.profiles.active"));
    }

}
