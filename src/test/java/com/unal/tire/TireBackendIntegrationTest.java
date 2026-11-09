package com.unal.tire;

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
import com.unal.tire.domain.valueobject.CondicionClimatica;
import com.unal.tire.infrastructure.adapter.out.persistence.SpringDataAlertaCriticaRepository;
import com.unal.tire.infrastructure.adapter.out.persistence.SpringDataConductorRepository;
import com.unal.tire.infrastructure.adapter.out.persistence.SpringDataUsuarioRepository;
import com.unal.tire.infrastructure.adapter.out.persistence.SpringDataViajeRepository;
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
 * Suite de integracion que verifica los escenarios de la guia del Prototipo 1:
 * 201, 200, 400, 401, 403, 404 y 409, mas el patron State de alerta y la
 * invariante HU-09 (un solo viaje activo por conductor).
 *
 * <p>Usa H2 en modo PostgreSQL (perfil {@code test}) y mockea el puerto de
 * clima para no depender de la red.</p>
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
    private SpringDataUsuarioRepository usuarioRepository;

    @Autowired
    private SpringDataConductorRepository conductorRepository;

    @Autowired
    private SpringDataViajeRepository viajeRepository;

    @Autowired
    private SpringDataAlertaCriticaRepository alertaRepository;

    private String despachadorToken;
    private String conductorToken;

    @BeforeEach
    void setUp() throws Exception {
        // Cada test arranca con la base limpia (el contexto se comparte).
        alertaRepository.deleteAll();
        viajeRepository.deleteAll();
        conductorRepository.deleteAll();
        usuarioRepository.deleteAll();

        when(consultorClimaPort.consultarClimaActual(anyDouble(), anyDouble()))
                .thenReturn(new CondicionClimatica(18.4, 12.0, "LLUVIA"));

        despachadorToken = registrarEIniciarSesion(
                "Maria Despachadora", "despachador@tire.com", "Password123", "DESPACHADOR");
        conductorToken = registrarEIniciarSesion(
                "Pedro Conductor", "conductor@tire.com", "Password123", "CONDUCTOR");
    }

    // ---- 2 y 4: registro + login JWT ----
    private String registrarEIniciarSesion(String nombre, String email, String password, String rol)
            throws Exception {
        String registro = """
                {"nombre":"%s","email":"%s","password":"%s","rol":"%s"}
                """.formatted(nombre, email, password, rol);

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.rol", is(rol)));

        String login = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/auth/sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tipo", is("Bearer")))
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("accessToken").asText();
    }

    // ---- 5: 401 sin token ----
    @Test
    void listarConductoresSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/conductores"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    // ---- 6: 403 rol insuficiente (conductor gestionando flota) ----
    @Test
    void conductorCreandoConductorDevuelve403() throws Exception {
        String body = """
                {"nombre":"X","documento":"CC-9","correo":"x@tire.com"}
                """;
        mockMvc.perform(post("/conductores")
                        .header("Authorization", "Bearer " + conductorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    // ---- 7: 400 validacion (correo vacio) ----
    @Test
    void crearConductorDatosInvalidosDevuelve400() throws Exception {
        String body = """
                {"nombre":"Pedro","documento":"CC-1","correo":""}
                """;
        mockMvc.perform(post("/conductores")
                        .header("Authorization", "Bearer " + despachadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // ---- 8, 9, 10, 17: CRUD conductor completo ----
    @Test
    void crudConductorCompleto() throws Exception {
        String crear = """
                {"nombre":"Pedro Conductor","documento":"CC-1024","correo":"pedro@tire.com","telefono":"3001234567"}
                """;
        MvcResult creado = mockMvc.perform(post("/conductores")
                        .header("Authorization", "Bearer " + despachadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crear))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo", is(true)))
                .andReturn();

        String conductorId = objectMapper.readTree(creado.getResponse().getContentAsString())
                .get("id").asText();

        // 409: documento duplicado
        mockMvc.perform(post("/conductores")
                        .header("Authorization", "Bearer " + despachadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crear))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        // GET por id -> 200
        mockMvc.perform(get("/conductores/" + conductorId)
                        .header("Authorization", "Bearer " + despachadorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(conductorId)));

        // PATCH -> 200
        mockMvc.perform(patch("/conductores/" + conductorId)
                        .header("Authorization", "Bearer " + despachadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telefono\":\"3009998888\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefono", is("3009998888")));

        // DELETE -> 200 con activo=false
        mockMvc.perform(delete("/conductores/" + conductorId)
                        .header("Authorization", "Bearer " + despachadorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo", is(false)));
    }

    // ---- 404 ----
    @Test
    void obtenerConductorInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/conductores/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + despachadorToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // ---- 11: despachador no puede iniciar viaje -> 403 ----
    @Test
    void despachadorIniciandoViajeDevuelve403() throws Exception {
        String body = """
                {"origen":"Bogota","destino":"Medellin"}
                """;
        mockMvc.perform(post("/viajes")
                        .header("Authorization", "Bearer " + despachadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    // ---- 12, 13, 14, 15, 16: ciclo de viaje + HU-09 + patron State ----
    @Test
    void cicloViajeTelemetriaEventoYEstadoCritico() throws Exception {
        String iniciar = """
                {"origen":"Bogota","destino":"Medellin","latitudInicial":4.711,"longitudInicial":-74.0721}
                """;
        MvcResult viaje = mockMvc.perform(post("/viajes")
                        .header("Authorization", "Bearer " + conductorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(iniciar))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("EN_CURSO")))
                .andExpect(jsonPath("$.nivelAlerta", is("NORMAL")))
                .andReturn();

        String viajeId = objectMapper.readTree(viaje.getResponse().getContentAsString())
                .get("id").asText();

        // 13: segundo viaje activo -> 409 (HU-09)
        mockMvc.perform(post("/viajes")
                        .header("Authorization", "Bearer " + conductorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(iniciar))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        // 14: telemetria -> 200 con clima enriquecido
        mockMvc.perform(post("/viajes/" + viajeId + "/telemetria")
                        .header("Authorization", "Bearer " + conductorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitud\":5.5353,\"longitud\":-73.3678}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ultimoClima.descripcion", is("LLUVIA")));

        // 15: microsueno 2.0s NO confirmado -> patron State pasa a CRITICO
        mockMvc.perform(post("/viajes/" + viajeId + "/eventos")
                        .header("Authorization", "Bearer " + conductorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitud\":5.5353,\"longitud\":-73.3678,"
                                + "\"duracionOjosCerradosSegundos\":2.0,\"confirmadoPorConductor\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nivelAlerta", is("CRITICO")));

        // 16: finalizar -> 200
        mockMvc.perform(patch("/viajes/" + viajeId + "/finalizar")
                        .header("Authorization", "Bearer " + conductorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("FINALIZADO")));
    }

    // ---- Alerta critica abierta automaticamente y visible para el despachador ----
    @Test
    void eventoCriticoAbreAlertaParaDespachador() throws Exception {
        String iniciar = """
                {"origen":"Bogota","destino":"Cali","latitudInicial":4.711,"longitudInicial":-74.0721}
                """;
        MvcResult viaje = mockMvc.perform(post("/viajes")
                        .header("Authorization", "Bearer " + conductorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(iniciar))
                .andExpect(status().isCreated())
                .andReturn();

        String viajeId = objectMapper.readTree(viaje.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(post("/viajes/" + viajeId + "/eventos")
                        .header("Authorization", "Bearer " + conductorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"duracionOjosCerradosSegundos\":2.0,\"confirmadoPorConductor\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nivelAlerta", is("CRITICO")));

        // El despachador ve la alerta abierta
        mockMvc.perform(get("/alertas")
                        .header("Authorization", "Bearer " + despachadorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].viajeId", is(viajeId)))
                .andExpect(jsonPath("$[0].atendida", is(false)));
    }
}
