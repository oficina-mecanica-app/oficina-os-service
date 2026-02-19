package br.com.fiap.oficina.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.fiap.oficina.infrastructure.persistence.entity.ServiceData;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceData, Long> {
}