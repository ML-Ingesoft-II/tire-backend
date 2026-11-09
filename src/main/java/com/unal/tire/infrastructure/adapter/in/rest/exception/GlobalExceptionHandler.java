package com.unal.tire.infrastructure.adapter.in.rest.exception;

import com.unal.tire.domain.exception.AccesoDenegadoDominioException;
import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.exception.ReglaNegocioException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Manejador global de excepciones que traduce errores de dominio e
 * infraestructura a respuestas HTTP estandar RFC 9457 ({@link ProblemDetail}).
 *
 * <p>Nunca expone stacktraces internos ni detalles sensibles en la respuesta
 * JSON: solo se devuelven los campos definidos por el estandar
 * (type, title, status, detail, instance) mas metadatos controlados.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI TYPE_BAD_REQUEST =
            URI.create("https://tire.unal.com/problems/bad-request");
    private static final URI TYPE_VALIDATION =
            URI.create("https://tire.unal.com/problems/validation-error");
    private static final URI TYPE_NOT_FOUND =
            URI.create("https://tire.unal.com/problems/not-found");
    private static final URI TYPE_CONFLICT =
            URI.create("https://tire.unal.com/problems/conflict");
    private static final URI TYPE_UNAUTHORIZED =
            URI.create("https://tire.unal.com/problems/unauthorized");
    private static final URI TYPE_FORBIDDEN =
            URI.create("https://tire.unal.com/problems/forbidden");
    private static final URI TYPE_INTERNAL =
            URI.create("https://tire.unal.com/problems/internal-error");

    // ---- 400 Bad Request: validacion de argumentos (@Valid) ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<FieldErrorDetail> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::toFieldErrorDetail)
                .toList();

        ProblemDetail problem = baseProblem(
                HttpStatus.BAD_REQUEST,
                "Error de validacion",
                "Uno o mas campos de la solicitud no son validos.",
                TYPE_VALIDATION,
                request);
        problem.setProperty("errors", errores);
        return problem;
    }

    // ---- 400 Bad Request: reglas de negocio y argumentos ilegales ----
    @ExceptionHandler({ReglaNegocioException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(RuntimeException ex, WebRequest request) {
        return baseProblem(
                HttpStatus.BAD_REQUEST,
                "Solicitud invalida",
                ex.getMessage(),
                TYPE_BAD_REQUEST,
                request);
    }

    // ---- 404 Not Found ----
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail handleNotFound(RecursoNoEncontradoException ex, WebRequest request) {
        return baseProblem(
                HttpStatus.NOT_FOUND,
                "Recurso no encontrado",
                ex.getMessage(),
                TYPE_NOT_FOUND,
                request);
    }

    // ---- 409 Conflict ----
    @ExceptionHandler(ConflictoEstadoException.class)
    public ProblemDetail handleConflict(ConflictoEstadoException ex, WebRequest request) {
        return baseProblem(
                HttpStatus.CONFLICT,
                "Conflicto de estado",
                ex.getMessage(),
                TYPE_CONFLICT,
                request);
    }

    // ---- 401 Unauthorized ----
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return baseProblem(
                HttpStatus.UNAUTHORIZED,
                "Credenciales invalidas",
                "Las credenciales proporcionadas no son validas.",
                TYPE_UNAUTHORIZED,
                request);
    }

    // ---- 403 Forbidden ----
    @ExceptionHandler({AccessDeniedException.class, AccesoDenegadoDominioException.class})
    public ProblemDetail handleForbidden(RuntimeException ex, WebRequest request) {
        return baseProblem(
                HttpStatus.FORBIDDEN,
                "Acceso denegado",
                "No tiene permisos para realizar esta operacion.",
                TYPE_FORBIDDEN,
                request);
    }

    // ---- 500 Internal Server Error (fallback, sin stacktrace) ----
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, WebRequest request) {
        return baseProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno",
                "Ocurrio un error inesperado. Intente nuevamente mas tarde.",
                TYPE_INTERNAL,
                request);
    }

    // ---- Helpers ----

    private static ProblemDetail baseProblem(
            HttpStatus status, String title, String detail, URI type, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(type);
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setInstance(resolveInstance(request));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    private static URI resolveInstance(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            String uri = servletWebRequest.getRequest().getRequestURI();
            if (uri != null && !uri.isBlank()) {
                return URI.create(uri);
            }
        }
        String description = request.getDescription(false);
        if (description != null && description.startsWith("uri=")) {
            return URI.create(description.substring(4));
        }
        return URI.create("about:blank");
    }

    private static FieldErrorDetail toFieldErrorDetail(FieldError fieldError) {
        String message = fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : "Valor invalido";
        return new FieldErrorDetail(fieldError.getField(), message);
    }

    /**
     * Detalle de un campo invalido incluido en la propiedad {@code errors}
     * de las respuestas de validacion.
     */
    public record FieldErrorDetail(String field, String message) {
    }
}
