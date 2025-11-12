package com.nhoclahola.socialnetworkv1.security;

import com.nhoclahola.socialnetworkv1.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider
{
    private static final String PRIVATE_KEY = "JWT_TOKEN";

    public static String generateJwtToken(User user)
    {
        return Jwts.builder()
                .setIssuer("nhoclahola")
                .setIssuedAt(new Date())
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .setExpiration(new Date(Long.MAX_VALUE))
                .signWith(SignatureAlgorithm.HS256, PRIVATE_KEY)
                .compact();
    }

    public static Claims introspect(String jwtToken)
    {
        // Remove Bearer prefix
        jwtToken = jwtToken.substring(7);
        return Jwts.parser()
                .setSigningKey(PRIVATE_KEY)
                .parseClaimsJws(jwtToken)
                .getBody();
    }
}
