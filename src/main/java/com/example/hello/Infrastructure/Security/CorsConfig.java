package com.example.hello.Infrastructure.Security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class CorsConfig {
    AppProperties appProperties;
    public static String BASE_URL;
    @Bean
    public CorsFilter corsFilter() {
        BASE_URL = appProperties.getFrontendUrl();

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                BASE_URL, appProperties.getBackendUrl(),
                "https://willa-unstaid-ardis.ngrok-free.dev",
                "https://nonnocturnal-unflappably-khalilah.ngrok-free.dev",
                "https://uncoagulative-tyrannisingly-eddie.ngrok-free.dev",
                "https://flavorsome-jule-regally.ngrok-free.dev",
                "https://denisha-interconvertible-squarishly.ngrok-free.dev"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
