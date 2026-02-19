package br.com.fiap.oficina.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.oficina.domain.model.OrderStatus;
import br.com.fiap.oficina.infrastructure.persistence.entity.ClientData;
import br.com.fiap.oficina.infrastructure.persistence.entity.Part;
import br.com.fiap.oficina.infrastructure.persistence.entity.PartItemData;
import br.com.fiap.oficina.infrastructure.persistence.entity.ServiceData;
import br.com.fiap.oficina.infrastructure.persistence.entity.ServiceOrderData;
import br.com.fiap.oficina.infrastructure.persistence.entity.VehicleData;
import br.com.fiap.oficina.infrastructure.persistence.repository.ClientRepository;
import br.com.fiap.oficina.infrastructure.persistence.repository.PartRepository;
import br.com.fiap.oficina.infrastructure.persistence.repository.ServiceOrderRepository;
import br.com.fiap.oficina.infrastructure.persistence.repository.ServiceRepository;
import br.com.fiap.oficina.infrastructure.persistence.repository.VehicleRepository;
import br.com.fiap.oficina.infrastructure.service.EmailService;
import br.com.fiap.oficina.presentation.dto.AddItemsDto;
import jakarta.transaction.Transactional;

@Service
public class ServiceOrderService {

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private  ServiceRepository serviceRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private EmailService emailService;

    public List<ServiceOrderData> listAll() {
        return serviceOrderRepository.findAll();
    }

    public Optional<ServiceOrderData> findById(Long id) {
        return serviceOrderRepository.findById(id);
    }

    public ServiceOrderData create(Long clientId, Long vehicleId) {
        ClientData client = clientRepository.findById(clientId)
                .orElseThrow(RuntimeException::new);

        VehicleData vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(RuntimeException::new);

        ServiceOrderData os = new ServiceOrderData();
        os.setClient(client);
        os.setVehicle(vehicle);
        os.setStatus(OrderStatus.RECEIVED);
        os.setCreatedAt(LocalDateTime.now());

        return serviceOrderRepository.save(os);
    }

    @Transactional
    public ServiceOrderData updateStatus(Long id, OrderStatus newStatus) {
        ServiceOrderData os = serviceOrderRepository.findById(id).orElseThrow();
        os.setStatus(newStatus);
        
        if (newStatus == OrderStatus.AWAITING_APPROVAL) {
            os.calculateTotalAmount();
            sendToBilling(os);
        }
        
        ServiceOrderData saved = serviceOrderRepository.save(os);

        if (newStatus != OrderStatus.AWAITING_APPROVAL) {
            emailService.sendStatusUpdateNotification(saved);
        }

        return saved;
    }

    private void sendToBilling(ServiceOrderData os) {
        Map<String, Object> message = new HashMap<>();
        message.put("id", os.getId());
        message.put("amount", os.getTotalAmount());
        message.put("clientEmail", os.getClient().getEmail());
        
        rabbitTemplate.convertAndSend("os-created-queue", message);
    }

    @Transactional
    public ServiceOrderData addItems(Long osId, AddItemsDto dto) {
        ServiceOrderData os = serviceOrderRepository.findById(osId)
                .orElseThrow(() -> new RuntimeException("OS não encontrada"));

        // 1. Adicionar Serviços
        if (dto.getServiceIds() != null) {
            if (os.getServices() == null) os.setServices(new ArrayList<>());
            
            for (Long serviceId : dto.getServiceIds()) {
                ServiceData service = serviceRepository.findById(serviceId)
                        .orElseThrow(() -> new RuntimeException("Serviço não encontrado: " + serviceId));
                os.getServices().add(service);
            }
        }

        // 2. Adicionar Peças
        if (dto.getItems() != null) {
            if (os.getParts() == null) os.setParts(new ArrayList<>());

            for (AddItemsDto.PartItemRequest itemDto : dto.getItems()) {
                Part partInStock = partRepository.findById(itemDto.getPartId())
                        .orElseThrow(() -> new RuntimeException("Peça não encontrada no estoque: " + itemDto.getPartId()));

                // Cria o item da OS baseado no estoque
                PartItemData orderItem = new PartItemData();
                orderItem.setName(partInStock.getName());
                orderItem.setPrice(partInStock.getPrice());
                orderItem.setQuantity(itemDto.getQuantity());

                os.getParts().add(orderItem);
            }
        }

        // Recalcula o total
        os.calculateTotalAmount();
        
        return serviceOrderRepository.save(os);
    }

    public void sendCheckoutEmail(Long osId, String paymentUrl) {
        ServiceOrderData os = serviceOrderRepository.findById(osId)
                .orElseThrow(() -> new RuntimeException("OS não encontrada"));

        emailService.sendPaymentLink(os, paymentUrl); 
    }
}