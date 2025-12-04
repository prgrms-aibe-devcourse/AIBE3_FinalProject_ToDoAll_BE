package com.server.global.config.security;

import com.server.global.config.security.jwt.JwtAuthenticationFilter;
import com.server.global.config.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Security 설정 (Swagger 허용 + JWT 필터 준비만)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/chat-test.html").permitAll()
                        .requestMatchers("/ws/**").permitAll() // WebSocket 엔드포인트 허용 (나중에 삭제)
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/chat-test.html",
                                "/ws/**",
                                "/actuator/**",
                                "/mcp", // mcp 관련 예방용 열어두기
                                "/mcp/**",
                                "/api/v1/users", // 회원가입
                                "/api/v1/auth/email-verifications/**", // 이메일 인증
                                "/api/v1/auth/token", // 로그인
                                "/api/v1/auth/password/**", // 비번 재설정
                                "/api/v1/auth/**", // 비로그인 비번 재설정
                                "/api/v1/resumes/**", // ES 테스트용 임시 허용 (나중에 삭제)
                                "/api/v1/jd/**", // ES 테스트용 임시 허용 (나중에 삭제)
                                "/api/v1/search/**", // ES 테스트용 임시 허용 (나중에 삭제)
                                "/api/v1/matches/**", // ES 테스트용 임시 허용 (나중에 삭제)
                                "/api/v1/dev/redis/**", // Redis 캐시 삭제용 임시 허용
                                "/api/v1/interviews/**", // AI 테스트용 임시 허용 (나중에 삭제)
                                "/api/v1/notifications/**", // SSE 관련 api 임시 허용 (나중에 삭제)
                                "/api/v1/files/**" // S3 관련 API 임시 허용 (나중에 삭제)
                        ).permitAll()
                        // preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/users/me").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // CORS 설정 - 모든 Origin, Header, Method 허용 (개발단계 기본)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // 인증정보 포함 허용 (쿠키 등)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}