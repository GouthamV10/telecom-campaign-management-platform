package com.telecom.campaign.user.service;

import com.telecom.campaign.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    private Key getSigningKey() {
        Key key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
        log.debug("Generated signing key for JWT");
        return key;
    }

    public String generateToken(User user) {
        log.info("Generating token for user={}", user.getEmail());

        String token = Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();

        log.debug("Token generated for user={}, expiresInMs={}", user.getEmail(), expiration);
        return token;
    }

    public String generateRefreshToken(User user) {
        log.info("Generating refresh token for user={}", user.getEmail());

        String refreshToken = Jwts.builder()
                .subject(user.getEmail())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();

        log.debug("Refresh token generated for user={}", user.getEmail());
        return refreshToken;
    }

    public String extractUsernameFromRefresh(String token) {
        try {
            String subject = Jwts.parser().verifyWith((SecretKey) getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
            log.debug("Extracted subject from refresh token");
            return subject;
        } catch (Exception e) {
            log.warn("Failed to extract username from refresh token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            var claims = Jwts.parser().verifyWith((SecretKey) getSigningKey()).build().parseSignedClaims(token).getPayload();
            String type = claims.get("type", String.class);
            return "refresh".equals(type) && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public long getExpiration() {
        return expiration;
    }

    public String extractUsername(String token){
        try{
            String subject = Jwts.parser().verifyWith((SecretKey) getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
            log.debug("Extracted subject from token: {}", subject);
            return subject;
        }catch(Exception e){
            log.warn("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token, User user){
        try{
            String subject = Jwts.parser().verifyWith((SecretKey) getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
            boolean valid = subject != null && subject.equals(user.getEmail()) && !isTokenExpired(token);
            log.debug("Token validity for user={} => {}", user.getEmail(), valid);
            return valid;
        }catch(Exception e){
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token){
        try{
            Date date = Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            boolean expired = date.before(new Date());
            log.debug("Token expired => {} (exp={})", expired, date);
            return expired;
        }catch(Exception e){
            log.warn("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }
}
