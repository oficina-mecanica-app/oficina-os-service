package br.com.fiap.oficina.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.oficina.infrastructure.persistence.entity.ServiceData;
import br.com.fiap.oficina.infrastructure.persistence.repository.ServiceRepository;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository repository;

    public ServiceData create(ServiceData service) {
        return repository.save(service);
    }

    public List<ServiceData> listAll() {
        return repository.findAll();
    }

    public Optional<ServiceData> findById(Long id) {
        return repository.findById(id);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}