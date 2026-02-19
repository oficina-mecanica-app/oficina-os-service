package br.com.fiap.oficina.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.com.fiap.oficina.application.service.ServiceOrderService;
import br.com.fiap.oficina.domain.model.OrderStatus;
import br.com.fiap.oficina.infrastructure.persistence.entity.ClientData;
import br.com.fiap.oficina.infrastructure.persistence.entity.ServiceOrderData;
import br.com.fiap.oficina.infrastructure.persistence.entity.VehicleData;
import br.com.fiap.oficina.presentation.dto.AddItemsDto;

@ExtendWith(MockitoExtension.class)
public class ServiceOrderControllerTest {

    @Mock
    private ServiceOrderService service;

    @InjectMocks
    private ServiceOrderController controller;

    @Test
    public void deveRetornarBadRequestAoCriarOsSemClienteOuVeiculo() {
        ResponseEntity<ServiceOrderData> response1 = controller.createOS(new ServiceOrderData());
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());

        ServiceOrderData osWithClient = new ServiceOrderData();
        osWithClient.setClient(new ClientData());
        ResponseEntity<ServiceOrderData> response2 = controller.createOS(osWithClient);
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());

        ServiceOrderData osWithVehicle = new ServiceOrderData();
        osWithVehicle.setVehicle(new VehicleData());
        ResponseEntity<ServiceOrderData> response3 = controller.createOS(osWithVehicle);
        assertEquals(HttpStatus.BAD_REQUEST, response3.getStatusCode());
    }

    @Test
    public void deveCriarOsComSucesso() {
        ServiceOrderData payload = new ServiceOrderData();
        ClientData client = new ClientData();
        client.setId(1L);
        VehicleData vehicle = new VehicleData();
        vehicle.setId(1L);
        payload.setClient(client);
        payload.setVehicle(vehicle);

        ServiceOrderData mockResponse = new ServiceOrderData();
        when(service.create(1L, 1L)).thenReturn(mockResponse);

        ResponseEntity<ServiceOrderData> response = controller.createOS(payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void deveListarTodasAsOs() {
        when(service.listAll()).thenReturn(List.of(new ServiceOrderData()));
        ResponseEntity<List<ServiceOrderData>> response = controller.listAll();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void deveBuscarOsPorId() {
        ServiceOrderData mockOs = new ServiceOrderData();
        when(service.findById(1L)).thenReturn(Optional.of(mockOs));
        ResponseEntity<ServiceOrderData> response = controller.findById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockOs, response.getBody());
    }

    @Test
    public void deveRetornarNotFoundAoBuscarOsPorIdInexistente() {
        when(service.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<ServiceOrderData> response = controller.findById(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void deveAtualizarStatusComSucesso() {
        ServiceOrderData mockOs = new ServiceOrderData();
        when(service.updateStatus(1L, OrderStatus.IN_PROGRESS)).thenReturn(mockOs);
        ResponseEntity<ServiceOrderData> response = controller.updateStatus(1L, "\"IN_PROGRESS\"");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockOs, response.getBody());
    }

    @Test
    public void deveRetornarNotFoundAoAtualizarStatusComErro() {
        when(service.updateStatus(eq(1L), any())).thenThrow(new RuntimeException());
        ResponseEntity<ServiceOrderData> response = controller.updateStatus(1L, "\"IN_PROGRESS\"");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void deveAdicionarItensComSucesso() {
        ServiceOrderData mockOs = new ServiceOrderData();
        AddItemsDto dto = new AddItemsDto();
        when(service.addItems(1L, dto)).thenReturn(mockOs);
        ResponseEntity<ServiceOrderData> response = controller.addItems(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockOs, response.getBody());
    }
}