package com.unal.tire.infrastructure.adapter.in.rest;

import com.unal.tire.application.port.in.AutenticacionUseCase;
import com.unal.tire.application.port.in.ComandoRegistro;
import com.unal.tire.application.port.in.RespuestaToken;
import com.unal.tire.application.port.out.ClaimsAutenticacion;
import com.unal.tire.domain.model.Usuario;
import com.unal.tire.infrastructure.adapter.in.rest.dto.CambioPasswordDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.LoginRequestDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.RecuperacionPasswordDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.RegistroRequestDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.RegistroResponseDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.TokenResponseDto;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para el modulo de autenticacion y acceso.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AutenticacionUseCase autenticacionUseCase;

    public AuthController(AutenticacionUseCase autenticacionUseCase) {
        this.autenticacionUseCase = autenticacionUseCase;
    }

    @PostMapping("/registro")
    public ResponseEntity<RegistroResponseDto> registrar(@Valid @RequestBody RegistroRequestDto request) {
        Usuario usuario = autenticacionUseCase.registrarUsuario(new ComandoRegistro(
                request.nombre(),
                request.email(),
                request.password(),
                request.rol(),
                request.flotaId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(RegistroResponseDto.desde(usuario));
    }

    @PostMapping("/sesion")
    public ResponseEntity<TokenResponseDto> iniciarSesion(@Valid @RequestBody LoginRequestDto request) {
        RespuestaToken respuesta = autenticacionUseCase.iniciarSesion(
                request.email(), request.password());
        return ResponseEntity.ok(TokenResponseDto.desde(respuesta));
    }

    @PutMapping("/contrasena")
    public ResponseEntity<Void> cambiarContrasena(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @Valid @RequestBody CambioPasswordDto request) {
        autenticacionUseCase.cambiarContrasena(
                principal.usuarioId(),
                request.passwordActual(),
                request.passwordNueva());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recuperacion")
    public ResponseEntity<Map<String, String>> recuperarContrasena(
            @Valid @RequestBody RecuperacionPasswordDto request) {
        autenticacionUseCase.solicitarRecuperacion(request.email());
        return ResponseEntity.ok(Map.of(
                "mensaje", "Si el correo está registrado, se enviaron las instrucciones de recuperación."));
    }
}
