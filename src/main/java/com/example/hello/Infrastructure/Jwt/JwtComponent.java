package com.example.hello.Infrastructure.Jwt;

import com.example.hello.Middleware.ParamName;
import com.example.hello.Enum.TokenName;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtComponent {
    JwtEncoder jwtEncoder;
    JwtDecoder jwtDecoder;

    public JwtComponent() throws Exception {
        // Đọc private.pem
        InputStream privateStream = new ClassPathResource("private.pem").getInputStream();
        String privateKeyContent = new String(privateStream.readAllBytes())
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] privateBytes = Base64.getDecoder().decode(privateKeyContent);
        var privateSpec = new PKCS8EncodedKeySpec(privateBytes);

        // Đọc public.pem
        InputStream pubStream = new ClassPathResource("public.pem").getInputStream();
        String publicKeyContent = new String(pubStream.readAllBytes())
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] publicBytes = Base64.getDecoder().decode(publicKeyContent);
        var publicSpec = new X509EncodedKeySpec(publicBytes);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(privateSpec);
        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(publicSpec);

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();

        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
        this.jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    public String generateToken(UUID userId, TokenName tokenName, UUID sessionId, Instant expiration) {
        var claims = JwtClaimsSet.builder()
                .claim(ParamName.USER_ID_JWT, userId)
                .claim(ParamName.TOKEN_NAME_JWT, tokenName)
                .claim(ParamName.SESSION_ID_JWT, sessionId)
                .issuedAt(Instant.now())
                .expiresAt(expiration)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public UUID getUserIdFromToken(String token) {
        Jwt jwt = this.jwtDecoder.decode(token);
        return UUID.fromString(jwt.getClaim(ParamName.USER_ID_JWT));
    }

    public TokenName getTokenNameFromToken(String token) {
        Jwt jwt = this.jwtDecoder.decode(token);
        return TokenName.valueOf(jwt.getClaim(ParamName.TOKEN_NAME_JWT));
    }

    public UUID getSessionIdFromToken(String token) {
        Jwt jwt = this.jwtDecoder.decode(token);
        return UUID.fromString(jwt.getClaim(ParamName.SESSION_ID_JWT));
    }

    public Instant getExpiredAfterFromToken(String token) {
        Jwt jwt = this.jwtDecoder.decode(token);
        return jwt.getExpiresAt();
    }
}
