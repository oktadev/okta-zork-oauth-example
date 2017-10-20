package com.okta.examples.zorkoauth.model.jwk;

import java.util.List;
import java.util.Optional;

public class JWKS {
    private List<JWK> keys;

    public List<JWK> getKeys() {
        return keys;
    }

    public void setKeys(List<JWK> keys) {
        this.keys = keys;
    }

    public Optional<JWK> getKeyByKid(String kid) {
        if (keys == null || kid == null) { return Optional.empty(); }
        return keys.stream().filter(key -> kid.equals(key.getKid())).findFirst();
    }
}
