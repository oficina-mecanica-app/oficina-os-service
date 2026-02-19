package br.com.fiap.oficina.presentation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.oficina.application.service.ServiceOrderService;
import br.com.fiap.oficina.domain.model.OrderStatus;
import br.com.fiap.oficina.infrastructure.persistence.entity.ServiceOrderData;
import br.com.fiap.oficina.presentation.dto.AddItemsDto;

@RestController
@RequestMapping("/os")
public class ServiceOrderController {

    @Autowired
    private ServiceOrderService service;

    @PostMapping
    public ResponseEntity<ServiceOrderData> createOS(@RequestBody ServiceOrderData payload) {
        if (payload.getClient() == null || payload.getVehicle() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.create(payload.getClient().getId(), payload.getVehicle().getId()));
    }

    @GetMapping
    public ResponseEntity<List<ServiceOrderData>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderData> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceOrderData> updateStatus(@PathVariable Long id, @RequestBody String status) {
        try {
            String cleanStatus = status.replaceAll("\"", "");
            OrderStatus newStatus = OrderStatus.valueOf(cleanStatus);
            ServiceOrderData updated = service.updateStatus(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/add-items")
    public ResponseEntity<ServiceOrderData> addItems(@PathVariable Long id, @RequestBody AddItemsDto dto) {
        return ResponseEntity.ok(service.addItems(id, dto));
    }
}