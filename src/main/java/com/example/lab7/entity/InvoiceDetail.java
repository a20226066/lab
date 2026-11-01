package com.example.lab7.entity;

import jakarta.persistence.*;

/**
 * Entidad que mapea la tabla "invoice_detail" (cada linea/producto del pedido).
 * Se guarda el precio del producto AL MOMENTO de la venta: si luego cambia el precio
 * en el catalogo, el pedido historico no se altera.
 */
@Entity
@Table(name = "invoice_detail")
public class InvoiceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Muchas lineas pertenecen a un pedido
    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    // Muchas lineas (de distintos pedidos) apuntan a un producto
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Precio unitario usado en la venta
    @Column(name = "price", nullable = false)
    private Double price;

    // subtotal = quantity x price (calculado en Java)
    @Column(name = "subtotal", nullable = false)
    private Double subtotal;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
