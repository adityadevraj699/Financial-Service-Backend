package com.Financial.service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.Financial.service.exception.JwtAuthException;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET;

    private Key getSignKey() {
        if (SECRET == null || SECRET.length() < 32) {
            throw new IllegalStateException("JWT secret key must be at least 32 characters");
        }
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String email, String role) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            return !getClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            throw new JwtAuthException("Token expired. Please login again.");
        } catch (SignatureException e) {
            throw new JwtAuthException("Invalid token signature.");
        } catch (MalformedJwtException e) {
            throw new JwtAuthException("Malformed token.");
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthException("Unsupported token format.");
        } catch (Exception e) {
            throw new JwtAuthException("Token validation failed: " + e.getMessage());
        }
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthException("Token has expired.");
        } catch (SignatureException e) {
            throw new JwtAuthException("Token signature is invalid.");
        } catch (MalformedJwtException e) {
            throw new JwtAuthException("Token is malformed.");
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthException("Token type is not supported.");
        } catch (IllegalArgumentException e) {
            throw new JwtAuthException("Token is null or empty.");
        }
    }
}