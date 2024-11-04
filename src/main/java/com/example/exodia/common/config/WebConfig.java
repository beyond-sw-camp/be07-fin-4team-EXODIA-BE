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

=======
      
>>>>>>> e6b9fcd147052383a4584fd7e04d91b1979d3a68
        config.addAllowedOriginPattern("https://*.exodiapot.xyz");
//        config.addAllowedOriginPattern("http://localhost:[8082,8087,8088,4443]");
        config.addAllowedOriginPattern("https://openvidu.exodiapot.xyz");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);

    }
<<<<<<< HEAD
}
=======
}
>>>>>>> e6b9fcd147052383a4584fd7e04d91b1979d3a68
