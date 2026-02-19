package br.com.fiap.oficina.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.fiap.oficina.infrastructure.persistence.entity.VehicleData;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleData, Long> {
    List<VehicleData> findByClientId(Long clientId);
}