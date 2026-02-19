package br.com.fiap.oficina.infrastructure.configuration;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.fiap.oficina.application.service.ServiceOrderService;
import br.com.fiap.oficina.domain.model.OrderStatus;

@Component
public class SagaConsumer {

    @Autowired
    private ServiceOrderService service;

    // --- 1. Pagamento Aprovado ---
    @RabbitListener(queues = "os-payment-approved-queue")
    public void handlePaymentApproved(Map<String, Object> message) {
        try {
            Long osId = Long.valueOf(message.get("serviceOrderId").toString());
            System.out.println("✅ Pagamento confirmado para OS " + osId);
            service.updateStatus(osId, OrderStatus.IN_PROGRESS);
        } catch (Exception e) {
            System.err.println("Erro ao processar pagamento aprovado: " + e.getMessage());
        }
    }

    // --- 2. Pagamento Recusado ---
    @RabbitListener(queues = "payment-refused-queue")
    public void handlePaymentRefused(Map<String, Object> message) {
        try {
            Long osId = Long.valueOf(message.get("serviceOrderId").toString());
            System.out.println("❌ Pagamento recusado para OS " + osId);
            service.updateStatus(osId, OrderStatus.REFUSED);
        } catch (Exception e) {
            System.err.println("Erro ao processar pagamento recusado: " + e.getMessage());
        }
    }

    // --- 3. Execução Finalizada ---
    @RabbitListener(queues = "execution-finished-queue")
    public void handleExecutionFinished(Map<String, Object> message) {
        try {
            System.out.println("📦 Mensagem recebida do Execution: " + message);

            Object idObj = message.get("serviceOrderId");
            if (idObj == null) idObj = message.get("osId");
            if (idObj == null) idObj = message.get("id");

            if (idObj == null) {
                throw new RuntimeException("Não achei nenhum ID válido no JSON: " + message);
            }

            Long osId = Long.valueOf(idObj.toString());
            System.out.println("🏁 Execução finalizada para OS " + osId);
            service.updateStatus(osId, OrderStatus.COMPLETED);
            
        } catch (Exception e) {
            System.err.println("Erro ao processar execução finalizada: " + e.getMessage());
        }
    }

    // --- 4. Link de Pagamento Gerado ---
    @RabbitListener(queues = "payment-link-generated-queue")
    public void handlePaymentLinkGenerated(Map<String, Object> message) {
        try {
            Long osId = Long.valueOf(message.get("serviceOrderId").toString());
            String paymentUrl = message.get("paymentUrl").toString();

            System.out.println("🔗 Link do Mercado Pago recebido para OS " + osId);
            
            service.sendCheckoutEmail(osId, paymentUrl); 
            
        } catch (Exception e) {
            System.err.println("Erro ao processar link de pagamento: " + e.getMessage());
        }
    }
}
