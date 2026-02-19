package br.com.fiap.oficina.infrastructure.configuration;

import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import br.com.fiap.oficina.infrastructure.persistence.repository.ServiceOrderRepository;
import br.com.fiap.oficina.domain.model.OrderStatus;

@Component
public class OSBillingConsumer {

    @Autowired
    private ServiceOrderRepository repository;

    @RabbitListener(queues = "payment-refused-queue")
    public void receivePaymentRefused(Map<String, Object> message) {
        try {
            Long osId = Long.valueOf(message.get("serviceOrderId").toString());

            repository.findById(osId).ifPresent(os -> {
                os.setStatus(OrderStatus.CANCELED);
                repository.save(os);
                System.out.println("⚠️ Saga Rollback: Pagamento recusado. OS " + osId + " cancelada.");
            });
        } catch (Exception e) {
            System.err.println("Erro ao processar rollback da OS: " + e.getMessage());
        }
    }
}