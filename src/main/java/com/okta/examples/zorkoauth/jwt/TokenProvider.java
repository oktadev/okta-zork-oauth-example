package com.okta.examples.zorkoauth.jwt;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.SigningKeyResolverAdapter;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    @Value("#{ @environment['security.oauth2.resource.openIdConfigUri'] }")
    private String openIdConfigUri;

    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);
    private static final String AUTHORITIES_KEY = "scp";

    Map<String, Object> jwks;

    @PostConstruct
    void setup() {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
        String jwksUri = null;
        try {
            Map<String, Object> discoveryDoc = mapper.readValue(simpleGet(openIdConfigUri), typeRef);
            jwksUri = (String) discoveryDoc.get("jwks_uri");
            jwks = mapper.readValue(simpleGet(jwksUri), typeRef);
        } catch (IOException e) {
            log.error("Failed to retreve jwks: {}", e.getMessage(), e);
            return;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = decodeJwt(token);

        String scopes = claims.get(AUTHORITIES_KEY).toString();
        scopes = scopes.substring(1, scopes.length() - 1).replace(" ", "");

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(scopes.split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        User principal = new User(claims.get("sub").toString(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String authToken) {
        return decodeJwt(authToken) != null;
    }

    public Claims decodeJwt(String jwt) {
        SigningKeyResolver resolver = new SigningKeyResolverAdapter() {
            public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
                try {
                    Map<String, String> jwk = getKeyById(jwks, jwsHeader.getKeyId());
                    BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.get("n")));
                    BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.get("e")));

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

    @SuppressWarnings("unchecked")
    private Map<String, String> getKeyById(Map<String, Object> jwks, String kid) {
        List<Map<String, String>> keys = (List<Map<String, String>>)jwks.get("keys");
        Map<String, String> ret = null;
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).get("kid").equals(kid)) {
                return keys.get(i);
            }
        }
        return ret;
    }

    private String simpleGet(String urlStr) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');

            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}