package com.unal.tire.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class FlotaTest {

    @Test
    void constructoresYGettersSetters_funcionanCorrectamente() {
        UUID id = UUID.randomUUID();
        Flota flota = new Flota(id, "Transportes Andinos");

        assertEquals(id, flota.getId());
        assertEquals("Transportes Andinos", flota.getNombre());

        UUID nuevoId = UUID.randomUUID();
        flota.setId(nuevoId);
        flota.setNombre("Nueva Flota");

        assertEquals(nuevoId, flota.getId());
        assertEquals("Nueva Flota", flota.getNombre());

        Flota vacia = new Flota();
        assertNotNull(vacia);
    }

    @Test
    void equalsAndHashCode_mismoId_sonIguales() {
        UUID id = UUID.randomUUID();
        Flota f1 = new Flota(id, "Flota A");
        Flota f2 = new Flota(id, "Flota B");
        Flota f3 = new Flota(UUID.randomUUID(), "Flota A");

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1, f3);
        assertNotEquals(f1, null);
        assertNotEquals(f1, "otro");
        assertEquals(f1, f1);
    }
}
