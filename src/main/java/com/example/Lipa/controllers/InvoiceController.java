package com.example.Lipa.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.Lipa.DTOs.BillingDtos;
import com.example.Lipa.services.InvoiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice retrieval and management")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<BillingDtos.InvoiceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(BillingDtos.InvoiceResponse.from(invoiceService.getInvoice(id)));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all invoices for a customer")
    public ResponseEntity<List<BillingDtos.InvoiceResponse>> listByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(
                invoiceService.getInvoicesForCustomer(customerId).stream()
                        .map(BillingDtos.InvoiceResponse::from)
                        .toList()
        );
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasRole('BILLING_ADMIN')")
    @Operation(summary = "Void an unpaid invoice")
    public ResponseEntity<BillingDtos.InvoiceResponse> voidInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(BillingDtos.InvoiceResponse.from(invoiceService.voidInvoice(id)));
    }
}