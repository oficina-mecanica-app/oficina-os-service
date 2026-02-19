package br.com.fiap.oficina.presentation.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import br.com.fiap.oficina.infrastructure.persistence.entity.ClientData;
import br.com.fiap.oficina.infrastructure.persistence.entity.VehicleData;

public class VehicleResponseTest {

    @Test
    public void deveRetornarNuloSeEntidadeForNula() {
        assertNull(VehicleResponse.fromEntity(null));
    }

    @Test
    public void deveMapearEntidadeComClienteNulo() {
        VehicleData entity = new VehicleData();
        entity.setId(1L);
        entity.setPlate("ABC-1234");
        entity.setModel("Fusca");
        entity.setBrand("VW");
        entity.setYear(1974);

        VehicleResponse response = VehicleResponse.fromEntity(entity);

        assertEquals(1L, response.id());
        assertEquals("ABC-1234", response.plate());
        assertEquals("Fusca", response.model());
        assertEquals("VW", response.brand());
        assertEquals(1974, response.year());
        assertNull(response.clientId());
        assertNull(response.clientName());
    }

    @Test
    public void deveMapearEntidadeComClientePreenchido() {
        ClientData client = new ClientData();
        client.setId(10L);
        client.setName("Lucas Dev");

        VehicleData entity = new VehicleData();
        entity.setId(1L);
        entity.setClient(client);

        VehicleResponse response = VehicleResponse.fromEntity(entity);

        assertEquals(1L, response.id());
        assertEquals(10L, response.clientId());
        assertEquals("Lucas Dev", response.clientName());
    }
}