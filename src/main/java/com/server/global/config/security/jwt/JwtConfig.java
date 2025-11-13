package com.server.global.config.security.jwt;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public String jwtSecret() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String secret = dotenv.get("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET이 .env에 정의되어 있지 않습니다.");
        }

        return secret;
    }
}
