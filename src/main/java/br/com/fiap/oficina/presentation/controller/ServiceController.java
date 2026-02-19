package br.com.fiap.oficina.presentation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.oficina.application.service.ServiceService;
import br.com.fiap.oficina.infrastructure.persistence.entity.ServiceData;

@RestController
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @PostMapping
    public ResponseEntity<ServiceData> create(@RequestBody ServiceData service) {
        return ResponseEntity.ok(serviceService.create(service));
    }

    @GetMapping
    public ResponseEntity<List<ServiceData>> listAll() {
        return ResponseEntity.ok(serviceService.listAll());
    }
}