package br.com.fiap.oficina.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.oficina.infrastructure.persistence.entity.ClientData;
import br.com.fiap.oficina.infrastructure.persistence.entity.VehicleData;
import br.com.fiap.oficina.infrastructure.persistence.repository.ClientRepository;
import br.com.fiap.oficina.infrastructure.persistence.repository.VehicleRepository;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ClientRepository clientRepository;

    public VehicleData create(VehicleData vehicle, Long clientId) {
        ClientData client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        vehicle.setClient(client);
        return vehicleRepository.save(vehicle);
    }

    public List<VehicleData> listAll() {
        return vehicleRepository.findAll();
    }

    public Optional<VehicleData> findById(Long id) {
        return vehicleRepository.findById(id);
    }

    public List<VehicleData> findByClient(Long clientId) {
         return vehicleRepository.findByClientId(clientId);
    }
}