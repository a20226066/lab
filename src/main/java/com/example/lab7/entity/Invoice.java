package com.example.lab7.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que mapea la tabla "invoice" (cabecera del pedido / comprobante simplificado).
 * Las validaciones del formulario estan en el DTO OrderForm; aqui solo se mapea la tabla.
 */
@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // "Factura" o "Boleta"
    @Column(name = "type", nullable = false)
    private String type;

    // Columna DATE -> LocalDate (solo fecha, ver guia de gestion de fechas)
    @Column(name = "date", nullable = false)
    private LocalDate date;

    // Muchos pedidos pertenecen a un cliente -> @ManyToOne + @JoinColumn(customer_id)
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Un pedido tiene muchas lineas de detalle (relacion inversa, la FK esta en invoice_detail)
    @OneToMany(mappedBy = "invoice")
    private List<InvoiceDetail> details = new ArrayList<>();

    /**
     * Total del pedido = suma de los subtotales de sus lineas (calculado en Java).
     * @Transient: no es una columna de la tabla.
     */
    @Transient
    public double getTotal() {
        double total = 0;
        for (InvoiceDetail d : details) {
            total += d.getSubtotal();
        }
        return total;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<InvoiceDetail> getDetails() {
        return details;
    }

    public void setDetails(List<InvoiceDetail> details) {
        this.details = details;
    }
}
