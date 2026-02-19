package br.com.fiap.oficina.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.com.fiap.oficina.infrastructure.persistence.entity.*;
import br.com.fiap.oficina.infrastructure.persistence.repository.*;
import br.com.fiap.oficina.presentation.dto.AddItemsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import br.com.fiap.oficina.domain.model.OrderStatus;
import br.com.fiap.oficina.infrastructure.service.EmailService;

@ExtendWith(MockitoExtension.class)
public class ServiceOrderServiceTest {

    @Mock
    private ServiceOrderRepository repository;

    @Mock
    private EmailService emailService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ServiceOrderService service;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private PartRepository partRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Test
    public void deveAtualizarOStatusDaOsComSucesso() {
        Long osId = 1L;

        ClientData mockClient = new ClientData();
        mockClient.setEmail("teste@exemplo.com");

        ServiceOrderData mockOs = new ServiceOrderData();
        mockOs.setId(osId);
        mockOs.setStatus(OrderStatus.RECEIVED);
        mockOs.setClient(mockClient);

        when(repository.findById(osId)).thenReturn(Optional.of(mockOs));
        when(repository.save(any(ServiceOrderData.class))).thenReturn(mockOs);

        service.updateStatus(osId, OrderStatus.AWAITING_APPROVAL);

        assertEquals(OrderStatus.AWAITING_APPROVAL, mockOs.getStatus());

        verify(repository, times(1)).save(mockOs);

        verify(rabbitTemplate, times(1)).convertAndSend(eq("os-created-queue"), any(Object.class));
    }

    @Test
    public void deveListarTodasAsOsComSucesso() {
        List<ServiceOrderData> listaMock = new ArrayList<>();
        listaMock.add(new ServiceOrderData());
        when(repository.findAll()).thenReturn(listaMock);

        List<ServiceOrderData> resultado = service.listAll();

        assertEquals(1, resultado.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void deveBuscarOsPorIdComSucesso() {
        Long osId = 1L;
        ServiceOrderData mockOs = new ServiceOrderData();
        mockOs.setId(osId);
        when(repository.findById(osId)).thenReturn(Optional.of(mockOs));

        Optional<ServiceOrderData> resultado = service.findById(osId);

        assertEquals(true, resultado.isPresent());
        assertEquals(osId, resultado.get().getId());
    }

    @Test
    public void deveCriarOsComSucesso() {
        Long clientId = 1L;
        Long vehicleId = 1L;

        ClientData mockClient = new ClientData();
        mockClient.setId(clientId);

        VehicleData mockVehicle = new VehicleData();
        mockVehicle.setId(vehicleId);

        ServiceOrderData mockOsSalva = new ServiceOrderData();
        mockOsSalva.setId(100L);
        mockOsSalva.setStatus(OrderStatus.RECEIVED);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(mockClient));
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
        when(repository.save(any(ServiceOrderData.class))).thenReturn(mockOsSalva);

        ServiceOrderData resultado = service.create(clientId, vehicleId);

        assertEquals(100L, resultado.getId());
        assertEquals(OrderStatus.RECEIVED, resultado.getStatus());
        verify(repository, times(1)).save(any(ServiceOrderData.class));
    }

    @Test
    public void deveAdicionarItensNaOsComSucesso() {
        Long osId = 1L;
        ServiceOrderData mockOs = new ServiceOrderData();
        mockOs.setId(osId);

        AddItemsDto dto = new AddItemsDto();
        dto.setServiceIds(java.util.List.of(2L)); // Adiciona 1 serviço

        AddItemsDto.PartItemRequest partDto = new AddItemsDto.PartItemRequest();
        partDto.setPartId(3L);
        partDto.setQuantity(2);
        dto.setItems(java.util.List.of(partDto)); // Adiciona 1 peça

        br.com.fiap.oficina.infrastructure.persistence.entity.ServiceData mockService = new ServiceData();
        mockService.setPrice(new java.math.BigDecimal("100.00"));

        Part mockPart = new Part();
        mockPart.setName("Filtro");
        mockPart.setPrice(new java.math.BigDecimal("50.00"));

        when(repository.findById(osId)).thenReturn(Optional.of(mockOs));
        when(serviceRepository.findById(2L)).thenReturn(Optional.of(mockService));
        when(partRepository.findById(3L)).thenReturn(Optional.of(mockPart));
        when(repository.save(any(ServiceOrderData.class))).thenReturn(mockOs);

        ServiceOrderData resultado = service.addItems(osId, dto);

        assertEquals(1, resultado.getServices().size());
        assertEquals(1, resultado.getParts().size());
        verify(repository, times(1)).save(mockOs);
    }

    @Test
    public void deveAtualizarStatusParaEmProgressoEEnviarNotificacao() {
        Long osId = 1L;
        ClientData mockClient = new ClientData();
        mockClient.setEmail("teste@exemplo.com");

        ServiceOrderData mockOs = new ServiceOrderData();
        mockOs.setId(osId);
        mockOs.setStatus(OrderStatus.RECEIVED);
        mockOs.setClient(mockClient);

        when(repository.findById(osId)).thenReturn(Optional.of(mockOs));
        when(repository.save(any(ServiceOrderData.class))).thenReturn(mockOs);

        service.updateStatus(osId, OrderStatus.IN_PROGRESS);

        assertEquals(OrderStatus.IN_PROGRESS, mockOs.getStatus());
        verify(emailService, times(1)).sendStatusUpdateNotification(mockOs);
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    public void deveIgnorarAdicaoDeItensQuandoDtoVierComListasNulas() {
        Long osId = 1L;
        ServiceOrderData mockOs = new ServiceOrderData();
        mockOs.setId(osId);

        br.com.fiap.oficina.presentation.dto.AddItemsDto dto = new br.com.fiap.oficina.presentation.dto.AddItemsDto();
        dto.setServiceIds(null);
        dto.setItems(null);

        when(repository.findById(osId)).thenReturn(Optional.of(mockOs));
        when(repository.save(any(ServiceOrderData.class))).thenReturn(mockOs);

        service.addItems(osId, dto);

        verify(repository, times(1)).save(mockOs);
    }

    @Test
    public void deveAdicionarItensEmOsQueJaPossuiListasInicializadas() {
        Long osId = 1L;
        ServiceOrderData mockOs = new ServiceOrderData();
        mockOs.setId(osId);
        mockOs.setServices(new java.util.ArrayList<>());
        mockOs.setParts(new java.util.ArrayList<>());

        br.com.fiap.oficina.presentation.dto.AddItemsDto dto = new br.com.fiap.oficina.presentation.dto.AddItemsDto();
        dto.setServiceIds(java.util.List.of(2L));

        br.com.fiap.oficina.presentation.dto.AddItemsDto.PartItemRequest partDto = new br.com.fiap.oficina.presentation.dto.AddItemsDto.PartItemRequest();
        partDto.setPartId(3L);
        partDto.setQuantity(2);
        dto.setItems(java.util.List.of(partDto));

        br.com.fiap.oficina.infrastructure.persistence.entity.ServiceData mockService = new br.com.fiap.oficina.infrastructure.persistence.entity.ServiceData();
        mockService.setPrice(new java.math.BigDecimal("100.00"));

        br.com.fiap.oficina.infrastructure.persistence.entity.Part mockPart = new br.com.fiap.oficina.infrastructure.persistence.entity.Part();
        mockPart.setName("Filtro");
        mockPart.setPrice(new java.math.BigDecimal("50.00"));

        when(repository.findById(osId)).thenReturn(Optional.of(mockOs));
        when(serviceRepository.findById(2L)).thenReturn(Optional.of(mockService));
        when(partRepository.findById(3L)).thenReturn(Optional.of(mockPart));
        when(repository.save(any(ServiceOrderData.class))).thenReturn(mockOs);

        service.addItems(osId, dto);

        verify(repository, times(1)).save(mockOs);
    }

    @Test
    public void deveEnviarEmailDeCheckoutComSucesso() {
        Long osId = 1L;
        String url = "http://mercadopago.com/link";
        ServiceOrderData mockOs = new ServiceOrderData();
        mockOs.setId(osId);

        when(repository.findById(osId)).thenReturn(Optional.of(mockOs));

        service.sendCheckoutEmail(osId, url);

        verify(emailService, times(1)).sendPaymentLink(mockOs, url);
    }

    @Test
    public void deveLancarExcecaoAoTentarEnviarEmailDeCheckoutParaOsInexistente() {
        Long osId = 99L;

        when(repository.findById(osId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.sendCheckoutEmail(osId, "http://mercadopago.com/link");
        });

        verify(emailService, never()).sendPaymentLink(any(), anyString());
    }

    @Test
    public void deveLancarExcecaoAoCriarOsComClienteInexistente() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.create(1L, 1L));
    }

    @Test
    public void deveLancarExcecaoAoCriarOsComVeiculoInexistente() {
        ClientData mockClient = new ClientData();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(mockClient));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.create(1L, 1L));
    }

    @Test
    public void deveLancarExcecaoAoAtualizarStatusDeOsInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateStatus(1L, OrderStatus.IN_PROGRESS));
    }

    @Test
    public void deveLancarExcecaoAoAdicionarItensEmOsInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.addItems(1L, new AddItemsDto()));
    }

    @Test
    public void deveLancarExcecaoAoAdicionarServicoInexistenteNaOs() {
        ServiceOrderData mockOs = new ServiceOrderData();
        when(repository.findById(1L)).thenReturn(Optional.of(mockOs));
        AddItemsDto dto = new AddItemsDto();
        dto.setServiceIds(java.util.List.of(99L));
        when(serviceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.addItems(1L, dto));
    }

    @Test
    public void deveLancarExcecaoAoAdicionarPecaInexistenteNaOs() {
        ServiceOrderData mockOs = new ServiceOrderData();
        when(repository.findById(1L)).thenReturn(Optional.of(mockOs));
        AddItemsDto dto = new AddItemsDto();
        AddItemsDto.PartItemRequest partDto = new AddItemsDto.PartItemRequest();
        partDto.setPartId(99L);
        dto.setItems(java.util.List.of(partDto));
        when(partRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.addItems(1L, dto));
    }
}