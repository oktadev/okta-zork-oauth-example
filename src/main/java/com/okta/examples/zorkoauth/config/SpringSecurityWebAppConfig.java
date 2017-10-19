package com.okta.examples.zorkoauth.config;


import com.okta.examples.zorkoauth.jwt.JWTFilter;
import com.okta.examples.zorkoauth.jwt.TokenProvider;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableOAuth2Sso
public class SpringSecurityWebAppConfig extends WebSecurityConfigurerAdapter {

    private TokenProvider tokenProvider;

    public SpringSecurityWebAppConfig(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public static final String VERSION = "/v1";

    @Override
    public void configure(HttpSecurity http) throws Exception {

        http
            .antMatcher("/**")
            .authorizeRequests()
            .antMatchers("/").permitAll()
            .antMatchers(VERSION + "/instructions").permitAll()
            .antMatchers(VERSION + "/game").fullyAuthenticated().and()
            .cors().and()
            .csrf().ignoringAntMatchers(VERSION + "/game").and()
            .addFilterBefore(new JWTFilter(tokenProvider), BasicAuthenticationFilter.class);
    }
}
