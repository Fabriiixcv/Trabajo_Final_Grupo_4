package com.upc.comparasalud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

//Genera y valida los tokens JWT. El token lleva el rol y el id de quien inicio sesion.
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiracion-minutos:60}")
    private long expiracionMinutos;

    private Key clave() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generarToken(String rol, Long id, String correo, String nombre) {
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(correo)
                .claim("rol", rol)
                .claim("id", id)
                .claim("nombre", nombre)
                .setIssuedAt(new Date(ahora))
                .setExpiration(new Date(ahora + expiracionMinutos * 60_000))
                .signWith(clave(), SignatureAlgorithm.HS512)
                .compact();
    }

    //Lanza JwtException si la firma no es valida o el token vencio
    public Claims leer(String token) {
        return Jwts.parserBuilder().setSigningKey(clave()).build().parseClaimsJws(token).getBody();
    }
}
