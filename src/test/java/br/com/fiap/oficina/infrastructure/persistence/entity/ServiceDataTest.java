package br.com.fiap.oficina.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class ServiceDataTest {

    @Test
    public void deveTestarGettersESetters() {
        ServiceData serviceData = new ServiceData();

        serviceData.setId(1L);
        serviceData.setName("Troca de Óleo");
        serviceData.setDescription("Troca completa de óleo sintético");
        serviceData.setPrice(new BigDecimal("150.00"));

        assertEquals(1L, serviceData.getId());
        assertEquals("Troca de Óleo", serviceData.getName());
        assertEquals("Troca completa de óleo sintético", serviceData.getDescription());
        assertEquals(new BigDecimal("150.00"), serviceData.getPrice());
    }

    @Test
    public void deveCriarInstanciaComConstrutorVazio() {
        ServiceData serviceData = new ServiceData();

        assertNull(serviceData.getId());
        assertNull(serviceData.getName());
        assertNull(serviceData.getDescription());
        assertNull(serviceData.getPrice());
    }
}