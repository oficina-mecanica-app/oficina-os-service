package br.com.fiap.oficina.presentation.dto;

import java.util.List;

import lombok.Data;

@Data
public class AddItemsDto {
    private List<Long> serviceIds;
    private List<PartItemRequest> items;

    @Data
    public static class PartItemRequest {
        private Long partId;
        private Integer quantity;
    }
}