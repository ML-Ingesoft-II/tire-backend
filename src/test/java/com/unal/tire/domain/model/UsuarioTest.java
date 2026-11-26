package com.unal.tire.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void cambiarPassword_actualizaHashYDesactivaDebeCambiarPassword() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario(
                id, "conductor@tandinos.co", "hashInicial", "Pedro",
                RolUsuario.CONDUCTOR, null, true, true
        );

        assertTrue(usuario.isDebeCambiarPassword());
        assertEquals("hashInicial", usuario.getPasswordHash());

        usuario.cambiarPassword("nuevoHashSeguro");

        assertFalse(usuario.isDebeCambiarPassword());
        assertEquals("nuevoHashSeguro", usuario.getPasswordHash());
    }

    @Test
    void gettersYSetters_funcionanCorrectamente() {
        UUID id = UUID.randomUUID();
        UUID flotaId = UUID.randomUUID();
        Usuario usuario = new Usuario(
                id, "laura@tandinos.co", "hash1", "Laura Gómez",
                RolUsuario.DESPACHADOR, flotaId, true
        );

        assertEquals(id, usuario.getId());
        assertEquals("laura@tandinos.co", usuario.getEmail());
        assertEquals("hash1", usuario.getPasswordHash());
        assertEquals("Laura Gómez", usuario.getNombre());
        assertEquals(RolUsuario.DESPACHADOR, usuario.getRol());
        assertEquals(flotaId, usuario.getFlotaId());
        assertTrue(usuario.isActivo());
        assertFalse(usuario.isDebeCambiarPassword());

        UUID nuevoId = UUID.randomUUID();
        UUID nuevaFlotaId = UUID.randomUUID();
        usuario.setId(nuevoId);
        usuario.setEmail("nuevo@tandinos.co");
        usuario.setPasswordHash("hash2");
        usuario.setNombre("Laura Modificada");
        usuario.setRol(RolUsuario.CONDUCTOR);
        usuario.setFlotaId(nuevaFlotaId);
        usuario.setActivo(false);
        usuario.setDebeCambiarPassword(true);

        assertEquals(nuevoId, usuario.getId());
        assertEquals("nuevo@tandinos.co", usuario.getEmail());
        assertEquals("hash2", usuario.getPasswordHash());
        assertEquals("Laura Modificada", usuario.getNombre());
        assertEquals(RolUsuario.CONDUCTOR, usuario.getRol());
        assertEquals(nuevaFlotaId, usuario.getFlotaId());
        assertFalse(usuario.isActivo());
        assertTrue(usuario.isDebeCambiarPassword());

        Usuario vacio = new Usuario();
        assertNotNull(vacio);
    }

    @Test
    void equalsAndHashCode_mismoId_sonIguales() {
        UUID id = UUID.randomUUID();
        Usuario u1 = new Usuario(id, "u1@tandinos.co", "h1", "U1", RolUsuario.CONDUCTOR, null, true);
        Usuario u2 = new Usuario(id, "u2@tandinos.co", "h2", "U2", RolUsuario.DESPACHADOR, null, false);
        Usuario u3 = new Usuario(UUID.randomUUID(), "u1@tandinos.co", "h1", "U1", RolUsuario.CONDUCTOR, null, true);

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
        assertNotEquals(u1, u3);
        assertNotEquals(u1, null);
        assertNotEquals(u1, "otro");
        assertEquals(u1, u1);
    }
}
