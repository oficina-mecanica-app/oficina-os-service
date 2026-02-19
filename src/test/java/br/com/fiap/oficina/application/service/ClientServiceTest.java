package br.com.fiap.oficina.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import br.com.fiap.oficina.infrastructure.persistence.repository.ClientRepository;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository repository;

    @InjectMocks
    private ClientService service;

    @Test
    public void deveListarTodosOsClientes() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of(new ClientData(), new ClientData()));

        // Act
        List<ClientData> result = service.listAll();

        // Assert
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void deveSalvarClienteComSucesso() {
        // Arrange
        ClientData mockClient = new ClientData();
        mockClient.setId(1L);
        mockClient.setName("Lucas");

        when(repository.save(any(ClientData.class))).thenReturn(mockClient);

        // Act
        ClientData result = service.save(new ClientData());

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Lucas", result.getName());
        verify(repository, times(1)).save(any(ClientData.class));
    }

    @Test
    public void deveBuscarClientePorId() {
        // Arrange
        ClientData mockClient = new ClientData();
        mockClient.setId(10L);

        when(repository.findById(10L)).thenReturn(Optional.of(mockClient));

        // Act
        Optional<ClientData> result = service.findById(10L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
        verify(repository, times(1)).findById(10L);
    }
}