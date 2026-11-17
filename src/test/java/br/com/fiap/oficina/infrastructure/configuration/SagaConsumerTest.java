package br.com.fiap.oficina.infrastructure.configuration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import br.com.fiap.oficina.application.service.ServiceOrderService;
import br.com.fiap.oficina.domain.model.OrderStatus;

@ExtendWith(MockitoExtension.class)
public class SagaConsumerTest {

    @Mock
    private ServiceOrderService service;

    @InjectMocks
    private SagaConsumer sagaConsumer;

    @Test
    public void deveTratarPagamentoAprovado() {
        Map<String, Object> message = new HashMap<>();
        message.put("serviceOrderId", 10L);

        sagaConsumer.handlePaymentApproved(message);

        verify(service, times(1)).updateStatus(10L, OrderStatus.IN_PROGRESS);
    }

    @Test
    public void deveTratarErroNoPagamentoAprovado() {
        Map<String, Object> message = new HashMap<>();

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> sagaConsumer.handlePaymentApproved(message));

        verify(service, never()).updateStatus(anyLong(), any());
    }

    @Test
    public void deveTratarPagamentoRecusado() {
        Map<String, Object> message = new HashMap<>();
        message.put("serviceOrderId", 20L);

        sagaConsumer.handlePaymentRefused(message);

        verify(service, times(1)).updateStatus(20L, OrderStatus.REFUSED);
    }

    @Test
    public void deveTratarExecucaoFinalizadaComChaveServiceOrderId() {
        Map<String, Object> message = new HashMap<>();
        message.put("serviceOrderId", 30L);

        sagaConsumer.handleExecutionFinished(message);

        verify(service, times(1)).updateStatus(30L, OrderStatus.COMPLETED);
    }

    @Test
    public void deveTratarExecucaoFinalizadaComChaveId() {
        Map<String, Object> message = new HashMap<>();
        message.put("id", 40L);

        sagaConsumer.handleExecutionFinished(message);

        verify(service, times(1)).updateStatus(40L, OrderStatus.COMPLETED);
    }

    @Test
    public void deveTratarErroNaExecucaoFinalizadaPorFaltaDeId() {
        Map<String, Object> message = new HashMap<>();

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> sagaConsumer.handleExecutionFinished(message));

        verify(service, never()).updateStatus(anyLong(), any());
    }

    @Test
    public void deveTratarLinkDePagamentoGerado() {
        Map<String, Object> message = new HashMap<>();
        message.put("serviceOrderId", 50L);
        message.put("paymentUrl", "http://mercadopago.com/checkout");

        sagaConsumer.handlePaymentLinkGenerated(message);

        verify(service, times(1)).sendCheckoutEmail(50L, "http://mercadopago.com/checkout");
    }

    @Test
    public void deveTratarErroNoLinkDePagamentoGerado() {
        Map<String, Object> message = new HashMap<>();
        message.put("serviceOrderId", 60L);

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> sagaConsumer.handlePaymentLinkGenerated(message));

        verify(service, never()).sendCheckoutEmail(anyLong(), anyString());
    }

    @Test
    public void deveTratarErroNoPagamentoRecusado() {
        Map<String, Object> message = new HashMap<>();

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> sagaConsumer.handlePaymentRefused(message));

        verify(service, never()).updateStatus(anyLong(), any());
    }

    @Test
    public void deveAceitarChaveOsIdNoPagamentoRecusado() {
        Map<String, Object> message = new HashMap<>();
        message.put("osId", 70L);

        sagaConsumer.handlePaymentRefused(message);

        verify(service, times(1)).updateStatus(70L, OrderStatus.REFUSED);
    }

    @Test
    public void deveRejeitarIdMalFormado() {
        Map<String, Object> message = new HashMap<>();
        message.put("serviceOrderId", "abc");

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> sagaConsumer.handlePaymentApproved(message));

        verify(service, never()).updateStatus(anyLong(), any());
    }

    @Test
    public void devePropagarFalhaDoServicoParaPermitirRetry() {
        Map<String, Object> message = new HashMap<>();
        message.put("serviceOrderId", 80L);
        org.mockito.Mockito.when(service.updateStatus(80L, OrderStatus.IN_PROGRESS))
                .thenThrow(new IllegalStateException("banco indisponível"));

        assertThrows(IllegalStateException.class,
                () -> sagaConsumer.handlePaymentApproved(message));
    }
}
