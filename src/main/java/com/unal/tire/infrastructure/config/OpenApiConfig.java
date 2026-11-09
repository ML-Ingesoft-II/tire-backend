package com.unal.tire.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de OpenAPI / Swagger UI.
 *
 * <p>Define el esquema de seguridad {@code bearerAuth} (JWT) y los metadatos
 * de la API expuestos por springdoc en {@code /swagger-ui.html}.</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI tireOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tire API - Prototipo 1")
                        .description("Backend del sistema Tire: prevencion de accidentes viales por "
                                + "microsuenos. Conecta la App Movil (CONDUCTOR) y el Panel Web "
                                + "(DESPACHADOR) bajo Arquitectura Hexagonal.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo Tire - Universidad Nacional de Colombia"))
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Introduce el token JWT emitido por POST /auth/sesion.")));
    }
}
