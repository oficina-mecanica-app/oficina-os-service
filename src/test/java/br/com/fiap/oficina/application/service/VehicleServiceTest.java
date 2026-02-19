package br.com.fiap.oficina.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.oficina.infrastructure.persistence.entity.ClientData;
import br.com.fiap.oficina.infrastructure.persistence.entity.VehicleData;
import br.com.fiap.oficina.infrastructure.persistence.repository.ClientRepository;
import br.com.fiap.oficina.infrastructure.persistence.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private VehicleService service;

    @Test
    public void deveCriarVeiculoComSucessoQuandoClienteExiste() {
        // Arrange
        Long clientId = 1L;
        ClientData mockClient = new ClientData();
        mockClient.setId(clientId);

        VehicleData vehicleToSave = new VehicleData();
        vehicleToSave.setPlate("ABC-1234");

        VehicleData savedVehicle = new VehicleData();
        savedVehicle.setId(100L);
        savedVehicle.setClient(mockClient);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(mockClient));
        when(vehicleRepository.save(any(VehicleData.class))).thenReturn(savedVehicle);

        // Act
        VehicleData result = service.create(vehicleToSave, clientId);

        // Assert
        assertEquals(100L, result.getId());
        assertEquals(clientId, result.getClient().getId());
        verify(clientRepository, times(1)).findById(clientId);
        verify(vehicleRepository, times(1)).save(vehicleToSave);
    }

    @Test
    public void deveLancarExcecaoQuandoClienteNaoExistirAoCriarVeiculo() {
        // Arrange
        Long invalidClientId = 99L;
        VehicleData vehicleToSave = new VehicleData();

        when(clientRepository.findById(invalidClientId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.create(vehicleToSave, invalidClientId);
        });

        assertEquals("Cliente não encontrado", exception.getMessage());

        // Garante que se o cliente não existe, o sistema NUNCA deve chamar o save do banco
        verify(vehicleRepository, never()).save(any(VehicleData.class));
    }

    @Test
    public void deveListarTodosOsVeiculos() {
        // Arrange
        when(vehicleRepository.findAll()).thenReturn(List.of(new VehicleData()));

        // Act
        List<VehicleData> result = service.listAll();

        // Assert
        assertEquals(1, result.size());
        verify(vehicleRepository, times(1)).findAll();
    }

    @Test
    public void deveBuscarVeiculoPorId() {
        // Arrange
        VehicleData mockVehicle = new VehicleData();
        mockVehicle.setId(5L);
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(mockVehicle));

        // Act
        Optional<VehicleData> result = service.findById(5L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getId());
    }

    @Test
    public void deveBuscarVeiculosPorClientId() {
        // Arrange
        Long clientId = 1L;
        when(vehicleRepository.findByClientId(clientId)).thenReturn(List.of(new VehicleData(), new VehicleData()));

        // Act
        List<VehicleData> result = service.findByClient(clientId);

        // Assert
        assertEquals(2, result.size());
        verify(vehicleRepository, times(1)).findByClientId(clientId);
    }
}