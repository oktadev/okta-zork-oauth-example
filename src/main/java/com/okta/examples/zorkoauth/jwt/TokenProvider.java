package com.okta.examples.zorkoauth.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.examples.zorkoauth.model.DiscoveryDoc;
import com.okta.examples.zorkoauth.model.jwk.JWK;
import com.okta.examples.zorkoauth.model.jwk.JWKS;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    @Value("#{ @environment['security.oauth2.resource.openIdConfigUri'] }")
    private String openIdConfigUri;

    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);
    private static final String AUTHORITIES_KEY = "scp";

    JWKS jwks;

    @PostConstruct
    void setup() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            DiscoveryDoc discoveryDoc = mapper.readValue(Request.Get(openIdConfigUri).execute().returnContent().asString(), DiscoveryDoc.class);
            String jwksUri = discoveryDoc.getJwksUri();
            jwks = mapper.readValue(Request.Get(jwksUri).execute().returnContent().asString(), JWKS.class);
        } catch (IOException e) {
            log.error("Failed to retreve jwks: {}", e.getMessage(), e);
        }
    }

    public Authentication authenticationFromToken(String token) {
        Claims claims = parseJwt(token);

        String scopes = claims.get(AUTHORITIES_KEY).toString();
        scopes = scopes.substring(1, scopes.length() - 1).replace(" ", "");

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(scopes.split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        User principal = new User(claims.get("sub").toString(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public Claims parseJwt(String jwt) {
        SigningKeyResolver resolver = new SigningKeyResolverAdapter() {

            public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
                try {
                    JWK jwk = jwks.getKeyByKid(jwsHeader.getKeyId())
                        .orElseThrow(() -> new InvalidKeySpecException("no key for: " + jwsHeader.getKeyId()));
                    BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getN()));
                    BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getE()));

                    return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    log.error("Failed to resolve key: {}", e.getMessage(), e);
                    return null;
                }
            }
        };

        try {
            Jws<Claims> jwsClaims = Jwts.parser()
                .setSigningKeyResolver(resolver)
                .parseClaimsJws(jwt);

            return jwsClaims.getBody();
        } catch (Exception e) {
            log.error("Couldn't parse jwt: {}", e.getMessage(), e);
            return null;
        }
    }
}