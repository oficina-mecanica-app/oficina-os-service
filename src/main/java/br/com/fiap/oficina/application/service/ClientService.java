package br.com.fiap.oficina.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.oficina.infrastructure.persistence.entity.ClientData;
import br.com.fiap.oficina.infrastructure.persistence.repository.ClientRepository;

@Service
public class ClientService {

    @Autowired
    private ClientRepository repository;

    public List<ClientData> listAll() {
        return repository.findAll();
    }

    public ClientData save(ClientData client) {
        return repository.save(client);
    }
    
    public Optional<ClientData> findById(Long id) {
        return repository.findById(id);
    }
}