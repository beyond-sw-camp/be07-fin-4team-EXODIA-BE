package com.example.exodia.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
<<<<<<< HEAD
        config.addAllowedOrigin("https://exodiapot.xyz");
        config.addAllowedOrigin("http://localhost:8082");
        config.addAllowedOrigin("http://localhost:8088");
        config.addAllowedOrigin("http://localhost:8087");
        config.addAllowedOrigin("http://localhost:4443");

=======
        config.addAllowedOriginPattern("https://*.exodiapot.xyz");
        config.addAllowedOriginPattern("http://localhost:[8082,8087,8088,4443]");
>>>>>>> 1e993792f5fe8599f84f3afb3ff79a74deae0796
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
<<<<<<< HEAD
}
=======

}
>>>>>>> 1e993792f5fe8599f84f3afb3ff79a74deae0796
