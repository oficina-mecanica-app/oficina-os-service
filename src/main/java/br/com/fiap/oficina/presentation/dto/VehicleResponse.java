package br.com.fiap.oficina.presentation.dto;

import br.com.fiap.oficina.infrastructure.persistence.entity.VehicleData;

public record VehicleResponse(
    Long id,
    String plate,
    String model,
    String brand,
    Integer year,
    Long clientId,
    String clientName
) {

    public static VehicleResponse fromEntity(VehicleData entity) {
        if (entity == null) return null;
        return new VehicleResponse(
            entity.getId(),
            entity.getPlate(),
            entity.getModel(),
            entity.getBrand(),
            entity.getYear(),
            entity.getClient() != null ? entity.getClient().getId() : null,
            entity.getClient() != null ? entity.getClient().getName() : null
        );
    }
}