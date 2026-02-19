package br.com.fiap.oficina.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.fiap.oficina.infrastructure.persistence.entity.ServiceOrderData;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrderData, Long> {
}