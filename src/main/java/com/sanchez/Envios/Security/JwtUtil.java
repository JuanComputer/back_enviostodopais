package com.sanchez.Envios.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // En producción guarda la clave en vault o env var y usa al menos 256 bits
    private final Key key = Keys.hmacShaKeyFor(System.getenv().getOrDefault("JWT_SECRET", "KriuukwxmL1ru0EgL7DtzuNTdeb98HAOBDFf3ioX9kgatJ03K8IEn719dcekpuXOMNNsgkXwnS1mgFm6QXLdodKE26JoDzYXXNsCVoID3VDThJCQ5BydtGHpuvMnAqsGvQXecblwNnz8IZtvJFaw6KK2IfaUo6eGSPwmPWWc8ZzojV8oMOKz6iLqUohHDYmVgkKhVMlgmZGLfh21mfvaY3JTg7JDOr8wI13DKgzZBsaDCV0GzzaevTHSTlrJDPqn").getBytes());

    private final long expirationMs = 1000L * 60 * 60 * 24; // 24h

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("role","ROLE_"+role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return c.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        Object r = c.get("role");
        return r == null ? null : r.toString();
    }
}
