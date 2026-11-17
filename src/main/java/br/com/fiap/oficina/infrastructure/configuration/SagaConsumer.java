package br.com.fiap.oficina.infrastructure.configuration;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.com.fiap.oficina.application.service.ServiceOrderService;
import br.com.fiap.oficina.domain.model.OrderStatus;

/**
 * Consumidores da saga de pagamento e execução.
 *
 * Tratamento de falhas:
 * - Nenhuma exceção é engolida: o listener tenta de novo com espera crescente
 *   (ver application.properties) e, esgotadas as tentativas, a mensagem vai para
 *   a DLQ do serviço com o motivo da falha nos headers (ver RabbitMQConfig).
 * - Mensagem inválida (sem ID, ID mal formado) gera AmqpRejectAndDontRequeueException,
 *   com o problema descrito na própria exceção.
 * - Reentregas são seguras: ServiceOrderService.updateStatus é idempotente.
 */
@Component
public class SagaConsumer {

    private static final Logger log = LoggerFactory.getLogger(SagaConsumer.class);

    private final ServiceOrderService service;

    public SagaConsumer(ServiceOrderService service) {
        this.service = service;
    }

    @RabbitListener(queues = "os-payment-approved-queue")
    public void handlePaymentApproved(Map<String, Object> message) {
        Long osId = extractOrderId(message);
        log.info("Pagamento aprovado para a OS {}", osId);
        service.updateStatus(osId, OrderStatus.IN_PROGRESS);
    }

    @RabbitListener(queues = "payment-refused-queue")
    public void handlePaymentRefused(Map<String, Object> message) {
        Long osId = extractOrderId(message);
        log.info("Pagamento recusado para a OS {}", osId);
        service.updateStatus(osId, OrderStatus.REFUSED);
    }

    @RabbitListener(queues = "execution-finished-queue")
    public void handleExecutionFinished(Map<String, Object> message) {
        Long osId = extractOrderId(message);
        log.info("Execução finalizada para a OS {}", osId);
        service.updateStatus(osId, OrderStatus.COMPLETED);
    }

    @RabbitListener(queues = "payment-link-generated-queue")
    public void handlePaymentLinkGenerated(Map<String, Object> message) {
        Long osId = extractOrderId(message);
        Object paymentUrl = message.get("paymentUrl");
        if (paymentUrl == null) {
            throw new AmqpRejectAndDontRequeueException("Mensagem sem paymentUrl: " + message);
        }
        log.info("Link de pagamento recebido para a OS {}", osId);
        service.sendCheckoutEmail(osId, paymentUrl.toString());
    }

    /**
     * Os produtores usam chaves diferentes para o ID da OS; aceita todas.
     */
    private Long extractOrderId(Map<String, Object> message) {
        for (String key : new String[] { "serviceOrderId", "osId", "id" }) {
            Object value = message.get(key);
            if (value != null) {
                try {
                    return Long.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    throw new AmqpRejectAndDontRequeueException("ID de OS inválido: " + value, e);
                }
            }
        }
        throw new AmqpRejectAndDontRequeueException("Mensagem sem ID de OS: " + message);
    }
}
