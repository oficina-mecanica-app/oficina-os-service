package br.com.fiap.oficina.infrastructure.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import br.com.fiap.oficina.domain.model.OrderStatus;
import br.com.fiap.oficina.infrastructure.persistence.entity.ClientData;
import br.com.fiap.oficina.infrastructure.persistence.entity.ServiceOrderData;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    public void naoDeveEnviarEmailSeClienteForNulo() {
        ServiceOrderData os = new ServiceOrderData();

        emailService.sendStatusUpdateNotification(os);
        emailService.sendPaymentLink(os, "http://link.com");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    public void naoDeveEnviarEmailSeEmailDoClienteForNulo() {
        ClientData client = new ClientData();
        client.setName("Lucas");

        ServiceOrderData os = new ServiceOrderData();
        os.setClient(client);

        emailService.sendStatusUpdateNotification(os);
        emailService.sendPaymentLink(os, "http://link.com");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    public void deveEnviarNotificacaoDeStatusComSucesso() {
        ClientData client = new ClientData();
        client.setName("Lucas");
        client.setEmail("lucas@teste.com");

        ServiceOrderData os = new ServiceOrderData();
        os.setId(1L);
        os.setStatus(OrderStatus.RECEIVED);
        os.setTotalAmount(new BigDecimal("150.00"));
        os.setClient(client);

        emailService.sendStatusUpdateNotification(os);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void deveEnviarLinkDePagamentoComSucesso() {
        ClientData client = new ClientData();
        client.setName("Lucas");
        client.setEmail("lucas@teste.com");

        ServiceOrderData os = new ServiceOrderData();
        os.setId(2L);
        os.setTotalAmount(new BigDecimal("300.00"));
        os.setClient(client);

        emailService.sendPaymentLink(os, "http://mercadopago.com/link");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void deveCapturarExcecaoSeOcorrerErroNoEnvio() {
        ClientData client = new ClientData();
        client.setName("Lucas");
        client.setEmail("lucas@teste.com");

        ServiceOrderData os = new ServiceOrderData();
        os.setId(3L);
        os.setStatus(OrderStatus.RECEIVED);
        os.setClient(client);

        doThrow(new RuntimeException("Falha no servidor SMTP")).when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendStatusUpdateNotification(os);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void deveEnviarNotificacaoSemValorTotal() {
        ClientData client = new ClientData();
        client.setEmail("lucas@teste.com");

        ServiceOrderData os = new ServiceOrderData();
        os.setId(1L);
        os.setStatus(OrderStatus.RECEIVED);
        os.setClient(client);
        os.setTotalAmount(null);

        emailService.sendStatusUpdateNotification(os);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void deveCapturarExcecaoNoEnvioDoLinkDePagamento() {
        ClientData client = new ClientData();
        client.setEmail("lucas@teste.com");

        ServiceOrderData os = new ServiceOrderData();
        os.setId(1L);
        os.setClient(client);

        doThrow(new RuntimeException("Simulando erro")).when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendPaymentLink(os, "http://link.com");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}