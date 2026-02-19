package br.com.fiap.oficina.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import br.com.fiap.oficina.application.service.ServiceOrderService;
import br.com.fiap.oficina.domain.model.OrderStatus;
import br.com.fiap.oficina.infrastructure.persistence.entity.ClientData;
import br.com.fiap.oficina.infrastructure.persistence.entity.ServiceOrderData;
import br.com.fiap.oficina.infrastructure.persistence.entity.VehicleData;
import br.com.fiap.oficina.infrastructure.persistence.repository.ClientRepository;
import br.com.fiap.oficina.infrastructure.persistence.repository.ServiceOrderRepository;
import br.com.fiap.oficina.infrastructure.persistence.repository.VehicleRepository;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

public class ServiceOrderSteps {

    private ClientRepository clientRepository = mock(ClientRepository.class);
    private VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private ServiceOrderRepository serviceOrderRepository = mock(ServiceOrderRepository.class);

    private ServiceOrderService serviceOrderService = new ServiceOrderService();

    private Long clientId;
    private Long vehicleId;
    private ServiceOrderData result;

    public ServiceOrderSteps() {
        ReflectionTestUtils.setField(serviceOrderService, "clientRepository", clientRepository);
        ReflectionTestUtils.setField(serviceOrderService, "vehicleRepository", vehicleRepository);
        ReflectionTestUtils.setField(serviceOrderService, "serviceOrderRepository", serviceOrderRepository);
    }

    @Dado("que existe um cliente com ID {long} e um veiculo com ID {long}")
    public void que_existe_um_cliente_com_id_e_um_veiculo_com_id(Long cId, Long vId) {
        this.clientId = cId;
        this.vehicleId = vId;

        ClientData client = new ClientData();
        client.setId(cId);

        VehicleData vehicle = new VehicleData();
        vehicle.setId(vId);

        when(clientRepository.findById(cId)).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(vId)).thenReturn(Optional.of(vehicle));
    }

    @Quando("eu solicito a criacao de uma Ordem de Servico para este cliente e veiculo")
    public void eu_solicito_a_criacao_de_uma_ordem_de_servico_para_este_cliente_e_veiculo() {
        ServiceOrderData mockSaved = new ServiceOrderData();
        mockSaved.setId(100L);
        mockSaved.setStatus(OrderStatus.RECEIVED);

        when(serviceOrderRepository.save(any(ServiceOrderData.class))).thenReturn(mockSaved);

        this.result = serviceOrderService.create(clientId, vehicleId);
    }

    @Entao("a Ordem de Servico e criada com status {string}")
    public void a_ordem_de_servico_e_criada_com_status(String status) {
        assertNotNull(this.result);
        assertEquals(OrderStatus.valueOf(status), this.result.getStatus());
    }
}