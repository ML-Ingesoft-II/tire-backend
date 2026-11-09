package com.unal.tire.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Handler de acceso denegado: se invoca cuando un usuario autenticado intenta
 * acceder a un recurso para el que no tiene autoridad (p. ej. un rol distinto).
 *
 * <p>Escribe una respuesta HTTP {@code 403 Forbidden} con
 * {@code Content-Type: application/problem+json} (RFC 9457).</p>
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final URI TYPE_FORBIDDEN =
            URI.create("https://tire.unal.com/problems/forbidden");

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ProblemDetailWriter.write(
                response,
                request,
                HttpStatus.FORBIDDEN,
                TYPE_FORBIDDEN,
                "Acceso denegado",
                "No tiene permisos para acceder a este recurso.",
                objectMapper);
    }
}
