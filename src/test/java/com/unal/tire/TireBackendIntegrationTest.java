package com.unal.tire;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unal.tire.application.port.out.ConsultorClimaPort;
import com.unal.tire.application.port.out.FlotaRepositoryPort;
import com.unal.tire.application.port.out.ProveedorAutenticacionPort;
import com.unal.tire.application.port.out.UsuarioRepositoryPort;
import com.unal.tire.domain.model.Flota;
import com.unal.tire.domain.model.Usuario;
import com.unal.tire.domain.valueobject.CondicionClimatica;
import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Suite de integracion del backend alineada al contrato OpenAPI v1.1.0.
 *
 * <p>Verifica: login con datos de flota, 401 sin token, 403 por rol, alta de
 * conductor (Usuario + PerfilConductor con UUID compartido), ciclo de viaje con
 * escalada a CRITICO (patron State) y apertura de alerta, y estado de flota del
 * despachador.</p>
 *
 * <p>Usa H2 en modo PostgreSQL (perfil {@code test}) y mockea el puerto de clima.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TireBackendIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsultorClimaPort consultorClimaPort;

    @Autowired
    private FlotaRepositoryPort flotaRepository;

    @Autowired
    private UsuarioRepositoryPort usuarioRepository;

    @Autowired
    private ProveedorAutenticacionPort auth;

    private String despachadorToken;
    private String conductorParticularToken;

    @BeforeEach
    void setUp() throws Exception {
        when(consultorClimaPort.consultarClimaActual(anyDouble(), anyDouble()))
                .thenReturn(new CondicionClimatica(18.4, 12.0, "LLUVIA"));

        // Flota + despachador (no hay endpoint publico para crearlos).
        Flota flota = flotaRepository.buscarPorNombre("Flota Test")
                .orElseGet(() -> flotaRepository.guardar(new Flota(UUID.randomUUID(), "Flota Test")));

        String emailDespachador = "despachador@tire.com";
        if (usuarioRepository.buscarPorEmail(emailDespachador).isEmpty()) {
            usuarioRepository.guardar(new Usuario(
                    UUID.randomUUID(), emailDespachador, auth.hashearContrasena("Password123"),
                    "Maria Despachadora", RolUsuario.DESPACHADOR, flota.getId(), true, false));
        }
        despachadorToken = iniciarSesion(emailDespachador, "Password123");

        // Conductor particular (registro publico).
        String emailConductor = "particular+" + UUID.randomUUID() + "@tire.com";
        String registro = """
                {"nombre":"Pedro Particular","email":"%s","password":"Password123","aceptaTratamientoDatos":true}
                """.formatted(emailConductor);
        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated());
        conductorParticularToken = iniciarSesion(emailConductor, "Password123");
    }

    private String iniciarSesion(String email, String password) throws Exception {
        String login = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
        MvcResult result = mockMvc.perform(post("/auth/sesion")
                        .contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("accessToken").asText();
    }

    @Test
    void loginDespachadorDevuelveDatosDeFlota() throws Exception {
        mockMvc.perform(post("/auth/sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"despachador@tire.com\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol", is("DESPACHADOR")))
                .andExpect(jsonPath("$.flotaId").exists())
                .andExpect(jsonPath("$.nombreFlota", is("Flota Test")))
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void listarConductoresSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/conductores"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    void conductorCreandoConductorDevuelve403() throws Exception {
        String body = """
                {"nombres":"X","apellidos":"Y","tipoDocumento":"CC","numeroDocumento":"1234","correo":"x@tire.com"}
                """;
        mockMvc.perform(post("/conductores")
                        .header("Authorization", "Bearer " + conductorParticularToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    void crearConductorValidoDevuelve201() throws Exception {
        String body = """
                {"nombres":"Hernán David","apellidos":"Quintero Salas","tipoDocumento":"CC",
                 "numeroDocumento":"1015448392","correo":"hquintero@tire.com","telefono":"3155550129",
                 "placaVehiculo":"KTM-640"}
                """;
        mockMvc.perform(post("/conductores")
                        .header("Authorization", "Bearer " + despachadorToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo", is(true)))
                .andExpect(jsonPath("$.nombreCompleto", is("Hernán David Quintero Salas")))
                .andExpect(jsonPath("$.placaVehiculo", is("KTM-640")));
    }

    @Test
    void crearConductorDatosInvalidosDevuelve400() throws Exception {
        String body = """
                {"nombres":"Pedro","apellidos":"","tipoDocumento":"CC","numeroDocumento":"1","correo":""}
                """;
        mockMvc.perform(post("/conductores")
                        .header("Authorization", "Bearer " + despachadorToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void obtenerConductorInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/conductores/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + despachadorToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void cicloViajeMicrosuenoSinRespuestaEscalaACritico() throws Exception {
        MvcResult viaje = mockMvc.perform(post("/viajes")
                        .header("Authorization", "Bearer " + conductorParticularToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitudInicial\":4.711,\"longitudInicial\":-74.0721}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("EN_CURSO")))
                .andExpect(jsonPath("$.nivelAlerta", is("NORMAL")))
                .andReturn();

        String viajeId = objectMapper.readTree(viaje.getResponse().getContentAsString())
                .get("id").asText();

        // Segundo viaje activo -> 409 (HU-09)
        mockMvc.perform(post("/viajes")
                        .header("Authorization", "Bearer " + conductorParticularToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict());

        // Microsueno sin respuesta -> CRITICO (patron State)
        String evento = """
                {"tipoEvento":"MICROSUENO","fechaHoraDeteccion":"2026-11-06T22:41:52",
                 "duracionSegundos":2.3,"respondio":false,"latitud":5.1462,"longitud":-73.6851}
                """;
        mockMvc.perform(post("/viajes/" + viajeId + "/eventos")
                        .header("Authorization", "Bearer " + conductorParticularToken)
                        .contentType(MediaType.APPLICATION_JSON).content(evento))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nivelAlerta", is("CRITICO")));

        // Finalizar -> FINALIZADO
        mockMvc.perform(patch("/viajes/" + viajeId + "/finalizar")
                        .header("Authorization", "Bearer " + conductorParticularToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("FINALIZADO")));
    }

    @Test
    void estadoFlotaDevuelveConteosParaDespachador() throws Exception {
        mockMvc.perform(get("/flota/estado")
                        .header("Authorization", "Bearer " + despachadorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteoPorEstado").exists())
                .andExpect(jsonPath("$.conductoresEnViaje").exists());
    }

    @Test
    void microsuenoMenorAUmbralDevuelve400() throws Exception {
        MvcResult viaje = mockMvc.perform(post("/viajes")
                        .header("Authorization", "Bearer " + conductorParticularToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated())
                .andReturn();
        String viajeId = objectMapper.readTree(viaje.getResponse().getContentAsString())
                .get("id").asText();

        String evento = """
                {"tipoEvento":"MICROSUENO","fechaHoraDeteccion":"2026-11-06T22:41:52",
                 "duracionSegundos":1.2,"respondio":false}
                """;
        mockMvc.perform(post("/viajes/" + viajeId + "/eventos")
                        .header("Authorization", "Bearer " + conductorParticularToken)
                        .contentType(MediaType.APPLICATION_JSON).content(evento))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void recuperacionContrasenaDevuelve200() throws Exception {
        mockMvc.perform(post("/auth/recuperacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"despachador@tire.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void renovarSesionConRefreshTokenValidoDevuelve200YNuevoToken() throws Exception {
        MvcResult login = mockMvc.perform(post("/auth/sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"despachador@tire.com\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String refreshToken = objectMapper.readTree(login.getResponse().getContentAsString())
                .get("refreshToken").asText();

        mockMvc.perform(post("/auth/renovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.rol", is("DESPACHADOR")));
    }

    @Test
    void listarConductoresConPaginacionYFiltroDevuelve200() throws Exception {
        mockMvc.perform(get("/conductores")
                        .header("Authorization", "Bearer " + despachadorToken)
                        .param("q", "Hernán")
                        .param("page", "0")
                        .param("size", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina", is(0)))
                .andExpect(jsonPath("$.tamano", is(8)))
                .andExpect(jsonPath("$.totalElementos").exists())
                .andExpect(jsonPath("$.contenido").isArray());
    }

    @Test
    void actualizarYDesactivarConductorConTokenDespachador() throws Exception {
        String correoUnico = "patch.test." + UUID.randomUUID() + "@tandinos.co";
        String docUnico = "99" + (System.currentTimeMillis() % 1000000);
        String body = """
                {"nombres":"Conductor","apellidos":"Prueba","tipoDocumento":"CC",
                 "numeroDocumento":"%s","correo":"%s","telefono":"3100000000",
                 "placaVehiculo":"AAA111"}
                """.formatted(docUnico, correoUnico);

        MvcResult res = mockMvc.perform(post("/conductores")
                        .header("Authorization", "Bearer " + despachadorToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        String condId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asText();

        // PATCH /conductores/{id}
        String patchBody = """
                {"telefono":"3198887766","placaVehiculo":"NEW789"}
                """;
        mockMvc.perform(patch("/conductores/" + condId)
                        .header("Authorization", "Bearer " + despachadorToken)
                        .contentType(MediaType.APPLICATION_JSON).content(patchBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefono", is("3198887766")))
                .andExpect(jsonPath("$.placaVehiculo", is("NEW789")));

        // DELETE /conductores/{id}
        mockMvc.perform(delete("/conductores/" + condId)
                        .header("Authorization", "Bearer " + despachadorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo", is(false)));
    }

    @Test
    void flujoAlertasCriticasGeneracionConsultaYAtencion() throws Exception {
        Flota flota = flotaRepository.buscarPorNombre("Flota Test").orElseThrow();
        UUID cId = UUID.randomUUID();
        String emailConductor = "cond.flota." + UUID.randomUUID() + "@tandinos.co";
        usuarioRepository.guardar(new Usuario(
                cId, emailConductor, auth.hashearContrasena("Password123"),
                "Conductor Flota Alerta", RolUsuario.CONDUCTOR, flota.getId(), true, false
        ));
        String conductorFlotaToken = iniciarSesion(emailConductor, "Password123");

        // 1. Iniciar viaje
        MvcResult resViaje = mockMvc.perform(post("/viajes")
                        .header("Authorization", "Bearer " + conductorFlotaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitudInicial\":4.711,\"longitudInicial\":-74.0721}"))
                .andExpect(status().isCreated())
                .andReturn();
        String vId = objectMapper.readTree(resViaje.getResponse().getContentAsString()).get("id").asText();

        // 2. Evento microsueno sin respuesta -> genera AlertaCritica en la flota
        String evento = """
                {"tipoEvento":"MICROSUENO","fechaHoraDeteccion":"2026-11-26T21:00:00",
                 "duracionSegundos":2.5,"respondio":false,"latitud":5.1462,"longitud":-73.6851}
                """;
        mockMvc.perform(post("/viajes/" + vId + "/eventos")
                        .header("Authorization", "Bearer " + conductorFlotaToken)
                        .contentType(MediaType.APPLICATION_JSON).content(evento))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nivelAlerta", is("CRITICO")));

        // 3. GET /alertas como despachador
        MvcResult resAlertas = mockMvc.perform(get("/alertas")
                        .header("Authorization", "Bearer " + despachadorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").exists())
                .andReturn();

        JsonNode alertasArr = objectMapper.readTree(resAlertas.getResponse().getContentAsString());
        String alertaId = alertasArr.get(0).get("id").asText();

        // 4. GET /alertas/{id}
        mockMvc.perform(get("/alertas/" + alertaId)
                        .header("Authorization", "Bearer " + despachadorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(alertaId)))
                .andExpect(jsonPath("$.lineaTiempo").isArray());

        // 5. PATCH /alertas/{id}/atender
        String atenderBody = """
                {"notas":"Atendida por el despachador en prueba de integracion"}
                """;
        mockMvc.perform(patch("/alertas/" + alertaId + "/atender")
                        .header("Authorization", "Bearer " + despachadorToken)
                        .contentType(MediaType.APPLICATION_JSON).content(atenderBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atendida", is(true)))
                .andExpect(jsonPath("$.notasDespachador", is("Atendida por el despachador en prueba de integracion")));
    }

    @Test
    void historialEventosConFiltrosYPaginacionDevuelve200() throws Exception {
        mockMvc.perform(get("/eventos")
                        .header("Authorization", "Bearer " + despachadorToken)
                        .param("page", "0")
                        .param("size", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina", is(0)))
                .andExpect(jsonPath("$.tamano", is(8)))
                .andExpect(jsonPath("$.totalElementos").exists())
                .andExpect(jsonPath("$.contenido").isArray());
    }
}
