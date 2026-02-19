package br.com.fiap.oficina.presentation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.oficina.infrastructure.persistence.entity.Part;
import br.com.fiap.oficina.infrastructure.persistence.repository.PartRepository;

@RestController
@RequestMapping("/parts")
public class PartController {

    @Autowired
    private PartRepository repository;

    @PostMapping
    public ResponseEntity<Part> create(@RequestBody Part part) {
        return ResponseEntity.ok(repository.save(part));
    }

    @GetMapping
    public ResponseEntity<List<Part>> listAll() {
        return ResponseEntity.ok(repository.findAll());
    }
}