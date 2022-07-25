package com.okta.examples.zorkoauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OktaSpringBootOauthExampleApplication {

    public static final String VERSION = "/v1";

    public static void main(String[] args) {
        SpringApplication.run(OktaSpringBootOauthExampleApplication.class, args);
    }
}