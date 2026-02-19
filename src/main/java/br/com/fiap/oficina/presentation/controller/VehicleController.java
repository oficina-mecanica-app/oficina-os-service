package br.com.fiap.oficina.presentation.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.oficina.application.service.VehicleService;
import br.com.fiap.oficina.infrastructure.persistence.entity.VehicleData;
import br.com.fiap.oficina.presentation.dto.VehicleResponse;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService service;

    @GetMapping
    public List<VehicleResponse> listAll() {
        return service.listAll().stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping("/{clientId}")
    public ResponseEntity<VehicleResponse> create(@PathVariable Long clientId, @RequestBody VehicleData vehicle) {
        VehicleData savedVehicle = service.create(vehicle, clientId);
        return ResponseEntity.ok(VehicleResponse.fromEntity(savedVehicle));
    }
}