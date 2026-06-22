package com.shabytes.backend.security;

import com.shabytes.backend.domain.UserAccount;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final Duration accessTokenTtl;

    public JwtService(
            @Value("${chathub.security.jwt-secret}") String jwtSecret,
            @Value("${chathub.security.access-token-ttl}") Duration accessTokenTtl
    ) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = accessTokenTtl;
    }

    public IssuedToken issue(UserAccount user) {
        var now = Instant.now();
        var expiresAt = now.plus(accessTokenTtl);

        var token = Jwts.builder().subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

}
