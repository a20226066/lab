package com.example.lab7.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto del formulario de pedido (@ModelAttribute "orderForm").
 * Se usa un DTO (y no la entidad Invoice directamente) porque el formulario
 * maneja datos que no son columnas de invoice: la lista de lineas y el id del cliente.
 * Con los datos ya validados, el servicio crea las entidades Invoice e InvoiceDetail.
 */
public class OrderForm {

    // Factura o Boleta
    @NotBlank(message = "Seleccione el tipo de comprobante")
    @Pattern(regexp = "Factura|Boleta", message = "El tipo debe ser Factura o Boleta")
    private String type;

    // Obligatoria y no futura. <input type="date"> envia yyyy-MM-dd (ISO)
    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    // Id del cliente elegido en el <select>
    @NotNull(message = "Debe seleccionar un cliente")
    private Integer customerId;

    // @Valid -> tambien se validan las anotaciones de cada OrderLineForm (validacion en cascada)
    @Valid
    @NotEmpty(message = "Debe incluir al menos un producto")
    private List<OrderLineForm> lines = new ArrayList<>();

    // Total calculado en Java (suma de subtotales)
    private Double total;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public List<OrderLineForm> getLines() {
        return lines;
    }

    public void setLines(List<OrderLineForm> lines) {
        this.lines = lines;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}
