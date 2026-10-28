package com.funewmanagementsystem.security;

import com.funewmanagementsystem.entity.SystemAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(SystemAccount account) {
        return Jwts.builder()
                .subject(account.getAccountName())
                .claim("role", account.getAccountRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key())
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }
    public boolean isTokenValid(String token, UserDetails userDetails){
        Claims c = parse(token);
        return c.getSubject().equals(userDetails.getUsername() )&& c.getExpiration().after(new Date());
    }
    private Claims parse(String token){
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }

}
