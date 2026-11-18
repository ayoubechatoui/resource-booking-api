package com.booking.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.booking.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "une-cle-de-test-assez-longue-pour-hs256-0123456789";
    private static final String AUTRE_SECRET = "une-autre-cle-de-test-tout-aussi-longue-9876543210";

    @Test
    void unJetonCreeEstRelisibleAvecLeBonEmailEtLeBonRole() {
        JwtService jwt = new JwtService(SECRET, 60);

        String token = jwt.createToken("sara@mail.com", Role.USER);
        Claims claims = jwt.readToken(token);

        assertThat(claims.getSubject()).isEqualTo("sara@mail.com");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    void unJetonSigneAvecUneAutreCleEstRefuse() {
        JwtService jwt = new JwtService(SECRET, 60);
        String faux = new JwtService(AUTRE_SECRET, 60).createToken("sara@mail.com", Role.ADMIN);

        assertThatThrownBy(() -> jwt.readToken(faux)).isInstanceOf(JwtException.class);
    }

    @Test
    void unJetonExpireEstRefuse() {
        JwtService jwt = new JwtService(SECRET, -1);
        String token = jwt.createToken("sara@mail.com", Role.USER);

        assertThatThrownBy(() -> jwt.readToken(token)).isInstanceOf(JwtException.class);
    }
}