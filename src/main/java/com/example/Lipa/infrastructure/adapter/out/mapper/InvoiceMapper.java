package com.example.Lipa.infrastructure.adapter.out.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.Lipa.entity.Invoice;
import com.example.Lipa.infrastructure.adapter.out.persistence.entity.InvoiceEntity;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {
    
    default Invoice toDomain(InvoiceEntity entity) {
        return Invoice.create(
                entity.getCustomer().getCustomerId(),
                entity.getSubscription().getSubscriptionId(),
                entity.getCurrency()
        );
    }

    @Mapping(target = "lineItemsJson", ignore = true)
    InvoiceEntity toEntity(Invoice invoice);
}