package com.example.hello.Infrastructure.Security;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class CorsConfig {
    public static String BASE_URL = "https://www.huyhoang271.id.vn";
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                BASE_URL, "https://myproject.huyhoang271.id.vn", "https://willa-unstaid-ardis.ngrok-free.dev",
                "https://nonnocturnal-unflappably-khalilah.ngrok-free.dev", "https://uncoagulative-tyrannisingly-eddie.ngrok-free.dev",
                "https://flavorsome-jule-regally.ngrok-free.dev", "https://denisha-interconvertible-squarishly.ngrok-free.dev"
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
