package br.com.fiap.oficina.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ClientDataTest {

    @Test
    public void deveTestarGettersESetters() {
        ClientData client = new ClientData();
        client.setId(1L);
        client.setName("Lucas");
        client.setDocument("12345678900");
        client.setEmail("lucas@teste.com");

        assertEquals(1L, client.getId());
        assertEquals("Lucas", client.getName());
        assertEquals("12345678900", client.getDocument());
        assertEquals("lucas@teste.com", client.getEmail());
    }

    @Test
    public void deveTestarConstrutorComParametros() {
        ClientData client = new ClientData("Lucas", "12345678900");

        assertEquals("Lucas", client.getName());
        assertEquals("12345678900", client.getDocument());
        assertNull(client.getId());
        assertNull(client.getEmail());
    }
}