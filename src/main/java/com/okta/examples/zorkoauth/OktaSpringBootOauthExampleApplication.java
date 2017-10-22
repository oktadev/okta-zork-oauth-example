package com.okta.examples.zorkoauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class OktaSpringBootOauthExampleApplication {

    @Value("#{ @environment['cors.allowed.origins'] }")
    String[] allowedOrigins;

    public static final String VERSION = "/v1";

    public static void main(String[] args) {
        SpringApplication.run(OktaSpringBootOauthExampleApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                if (allowedOrigins != null && allowedOrigins.length > 0) {
                    registry.addMapping(VERSION + "/game").allowedOrigins(allowedOrigins);
                }
            }
        };
    }

    @Bean
    protected ResourceServerConfigurerAdapter resourceServerConfigurerAdapter() {

        return new ResourceServerConfigurerAdapter() {

            @Override
            public void configure(HttpSecurity http) throws Exception {
                http
                    .authorizeRequests()
                    .antMatchers("/", "/images/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                    .antMatchers(HttpMethod.OPTIONS,VERSION + "/game").permitAll();
            }
        };
    }
}