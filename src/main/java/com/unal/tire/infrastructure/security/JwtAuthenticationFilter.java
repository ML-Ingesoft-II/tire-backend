package com.unal.tire.infrastructure.security;

import com.unal.tire.application.port.out.ClaimsAutenticacion;
import com.unal.tire.application.port.out.ProveedorAutenticacionPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que extrae el token Bearer del header {@code Authorization}, lo valida
 * mediante {@link ProveedorAutenticacionPort} y, si es valido, inyecta la
 * autenticacion en el {@link SecurityContextHolder}.
 *
 * <p>El principal es el {@link ClaimsAutenticacion} y la autoridad concedida es
 * {@code ROLE_<rol>} (p. ej. {@code ROLE_CONDUCTOR}).</p>
 *
 * <p>Si el token es invalido/vencido se delega en
 * {@link CustomAuthenticationEntryPoint} para responder {@code 401} en formato
 * RFC 9457, sin propagar la peticion a la cadena.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ProveedorAutenticacionPort proveedorAutenticacion;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
            ProveedorAutenticacionPort proveedorAutenticacion,
            CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.proveedorAutenticacion = proveedorAutenticacion;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extraerToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            ClaimsAutenticacion claims = proveedorAutenticacion.validarToken(token);

            var authority = new SimpleGrantedAuthority("ROLE_" + claims.rol().name());
            var authentication = new UsernamePasswordAuthenticationToken(
                    claims, null, List.of(authority));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (TokenInvalidoException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new org.springframework.security.authentication.BadCredentialsException(
                            "Token invalido o expirado", ex));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
