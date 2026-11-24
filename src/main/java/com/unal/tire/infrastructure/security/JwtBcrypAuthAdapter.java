package com.unal.tire.infrastructure.security;

import com.unal.tire.application.port.out.ClaimsAutenticacion;
import com.unal.tire.application.port.out.ProveedorAutenticacionPort;
import com.unal.tire.domain.model.Usuario;
import com.unal.tire.domain.valueobject.RolUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Patron GoF <b>Adapter</b>: adapta {@link BCryptPasswordEncoder} de Spring
 * Security y la libreria {@code io.jsonwebtoken} (JJWT 0.12.5) al contrato de
 * dominio {@link ProveedorAutenticacionPort}.
 *
 * <p>La llave HMAC-SHA se deriva de la propiedad {@code jwt.secret}.</p>
 */
@Component
public class JwtBcrypAuthAdapter implements ProveedorAutenticacionPort {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_FLOTA_ID = "flotaId";
    private static final String CLAIM_DEBE_CAMBIAR = "debeCambiarPassword";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_REFRESH = "refresh";

    private final BCryptPasswordEncoder passwordEncoder;
    private final SecretKey signingKey;
    private final long expiracionMs;
    private final long refreshExpiracionMs;

    public JwtBcrypAuthAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expiracionMs,
            @Value("${jwt.refresh-expiration-ms:2592000000}") long refreshExpiracionMs) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
        this.refreshExpiracionMs = refreshExpiracionMs;
    }

    @Override
    public String hashearContrasena(String passwordPlano) {
        return passwordEncoder.encode(passwordPlano);
    }

    @Override
    public boolean verificarContrasena(String passwordPlano, String passwordHash) {
        if (passwordPlano == null || passwordHash == null) {
            return false;
        }
        return passwordEncoder.matches(passwordPlano, passwordHash);
    }

    @Override
    public String emitirToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim(CLAIM_EMAIL, usuario.getEmail())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .claim(CLAIM_FLOTA_ID, usuario.getFlotaId() != null ? usuario.getFlotaId().toString() : null)
                .claim(CLAIM_DEBE_CAMBIAR, usuario.isDebeCambiarPassword())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String emitirRefreshToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + refreshExpiracionMs);

        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public ClaimsAutenticacion validarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
                throw new TokenInvalidoException("No se admite un refresh token como access token", null);
            }

            UUID usuarioId = UUID.fromString(claims.getSubject());
            String email = claims.get(CLAIM_EMAIL, String.class);
            RolUsuario rol = RolUsuario.valueOf(claims.get(CLAIM_ROL, String.class));
            String flotaIdStr = claims.get(CLAIM_FLOTA_ID, String.class);
            UUID flotaId = flotaIdStr != null ? UUID.fromString(flotaIdStr) : null;
            Boolean debeCambiar = claims.get(CLAIM_DEBE_CAMBIAR, Boolean.class);

            return new ClaimsAutenticacion(usuarioId, email, rol, flotaId,
                    Boolean.TRUE.equals(debeCambiar));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new TokenInvalidoException("Token invalido o expirado", ex);
        }
    }

    @Override
    public UUID validarRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
                throw new TokenInvalidoException("El token no es un refresh token", null);
            }
            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new TokenInvalidoException("Refresh token invalido o expirado", ex);
        }
    }
}
