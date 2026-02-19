package br.com.fiap.oficina.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.oficina.infrastructure.persistence.entity.Part;

public interface PartRepository extends JpaRepository<Part, Long> {
}