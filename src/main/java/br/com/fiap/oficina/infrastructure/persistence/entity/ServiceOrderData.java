package br.com.fiap.oficina.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.fiap.oficina.domain.model.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "service_orders")
@Data
public class ServiceOrderData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ClientData client;

    @ManyToOne
    private VehicleData vehicle;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;

    @ManyToMany
    private List<ServiceData> services;

    @OneToMany(cascade = CascadeType.ALL)
    private List<PartItemData> parts;

    public void calculateTotalAmount() {
        BigDecimal servicesTotal = services != null ? 
            services.stream().map(ServiceData::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
        
        BigDecimal partsTotal = parts != null ? 
            parts.stream().map(p -> p.getPrice().multiply(new BigDecimal(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
        
        this.totalAmount = servicesTotal.add(partsTotal);
    }
}