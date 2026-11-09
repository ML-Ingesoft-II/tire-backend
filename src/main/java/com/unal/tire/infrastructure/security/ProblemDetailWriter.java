package com.unal.tire.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

/**
 * Helper para serializar respuestas de error con formato RFC 9457
 * ({@link ProblemDetail}) directamente sobre la {@link HttpServletResponse},
 * usado por los manejadores de seguridad que operan fuera del
 * {@code @RestControllerAdvice}.
 */
final class ProblemDetailWriter {

    private ProblemDetailWriter() {
    }

    static void write(
            HttpServletResponse response,
            HttpServletRequest request,
            HttpStatus status,
            URI type,
            String title,
            String detail,
            ObjectMapper objectMapper) throws IOException {

        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(type);
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
