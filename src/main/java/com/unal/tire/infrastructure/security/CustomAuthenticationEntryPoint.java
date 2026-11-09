package com.unal.tire.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Entry point de autenticacion: se invoca cuando una peticion no autenticada
 * (sin token o con token invalido/vencido) intenta acceder a un recurso
 * protegido.
 *
 * <p>Escribe una respuesta HTTP {@code 401 Unauthorized} con
 * {@code Content-Type: application/problem+json} (RFC 9457).</p>
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final URI TYPE_UNAUTHORIZED =
            URI.create("https://tire.unal.com/problems/unauthorized");

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ProblemDetailWriter.write(
                response,
                request,
                HttpStatus.UNAUTHORIZED,
                TYPE_UNAUTHORIZED,
                "No autenticado",
                "Se requiere un token de acceso valido para acceder a este recurso.",
                objectMapper);
    }
}
