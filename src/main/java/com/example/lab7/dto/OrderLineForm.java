package com.example.lab7.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Una linea del formulario de pedido: producto + cantidad.
 * price y subtotal NO vienen del formulario: los calcula el controlador en Java
 * (estan bloqueados para el databinding con @InitBinder en OrderController).
 */
public class OrderLineForm {

    @NotNull(message = "Seleccione un producto")
    private Integer productId;

    // Entero mayor que cero. Si escriben letras -> typeMismatch (messages.properties)
    @NotNull(message = "Ingrese la cantidad")
    @Min(value = 1, message = "La cantidad debe ser un entero mayor que cero")
    private Integer quantity;

    // ----- Calculados en Java (solo lectura en la vista) -----
    private Double price;
    private Double subtotal;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
