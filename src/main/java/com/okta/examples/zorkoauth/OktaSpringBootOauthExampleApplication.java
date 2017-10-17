package com.okta.examples.zorkoauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class OktaSpringBootOauthExampleApplication {

    @Value("#{ @environment['cors.allowed.origins'] }")
    String[] allowedOrigins;

    public static void main(String[] args) {
		SpringApplication.run(OktaSpringBootOauthExampleApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/v1/c").allowedOrigins(allowedOrigins);
			}
		};
	}
}
