package com.okta.examples.zorkoauth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("#{ @environment['cors.allowed.origins'] }")
    List<String> allowedOrigins;


    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .cors()
            .and()
            .headers().frameOptions().sameOrigin()
            .and()
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET","POST"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
