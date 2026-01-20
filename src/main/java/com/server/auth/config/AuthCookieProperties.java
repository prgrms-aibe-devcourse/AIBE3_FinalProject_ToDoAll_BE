package com.server.auth.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.cookie.auth")
public class AuthCookieProperties {

    private boolean secure;
    private String sameSite;
    private String path;
    private long accessMaxAgeSeconds;
    private long refreshMaxAgeSeconds;

    @PostConstruct // 앱 뜰 때 딱 1번 찍힘 (등록+바인딩 검증)
    public void logLoadedValues() {
        log.info("[AuthCookieProperties] loaded. secure={}, sameSite={}, path={}, accessMaxAgeSeconds={}, refreshMaxAgeSeconds={}",
                secure, sameSite, path, accessMaxAgeSeconds, refreshMaxAgeSeconds);
    }
}

