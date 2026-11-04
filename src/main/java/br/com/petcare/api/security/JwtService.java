package br.com.petcare.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Gera e valida os dois tokens da API:
 * - access token (curta duração) usado em toda requisição protegida;
 * - refresh token (longa duração) usado apenas em /api/auth/refresh para renovar a sessão.
 */
@Service
public class JwtService {

    private static final String CLAIM_TIPO = "tipo";
    private static final String TIPO_ACCESS = "access";
    private static final String TIPO_REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(UserDetails userDetails) {
        return construir(userDetails, expirationMs, TIPO_ACCESS);
    }

    public String gerarRefreshToken(UserDetails userDetails) {
        return construir(userDetails, refreshExpirationMs, TIPO_REFRESH);
    }

    private String construir(UserDetails userDetails, long duracaoMs, String tipo) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .claim(CLAIM_TIPO, tipo)
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + duracaoMs))
                .signWith(getSigningKey())
                .compact();
    }

    /** Lança JwtException se o token for inválido, adulterado ou expirado. */
    public String extrairEmail(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    public boolean ehAccessToken(String token) {
        return TIPO_ACCESS.equals(extrairClaim(token, claims -> claims.get(CLAIM_TIPO, String.class)));
    }

    public boolean ehRefreshToken(String token) {
        return TIPO_REFRESH.equals(extrairClaim(token, claims -> claims.get(CLAIM_TIPO, String.class)));
    }

    public boolean tokenValido(String token, UserDetails userDetails) {
        try {
            return extrairEmail(token).equals(userDetails.getUsername());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // O parser já rejeita tokens expirados (ExpiredJwtException é uma JwtException).
    private <T> T extrairClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
