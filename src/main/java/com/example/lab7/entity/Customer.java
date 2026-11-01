package com.example.lab7.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Entidad que mapea la tabla "customer".
 * Las validaciones (Hibernate Validator) se ejecutan cuando el controlador usa @Valid.
 */
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id AUTO_INCREMENT
    private Integer id;

    // Nombre obligatorio (no nulo ni vacio) y maximo 100 caracteres (VARCHAR(100))
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    @Column(name = "name", nullable = false)
    private String name;

    // Numero de documento. La regla "DNI = 8 digitos / RUC = 11 digitos" depende de OTRO campo
    // (documentType), por eso se valida en el controlador con bindingResult.rejectValue(...)
    @NotBlank(message = "El número de documento es obligatorio")
    @Column(name = "document", nullable = false, unique = true)
    private String document;

    // Tipo de documento: obligatorio y solo puede ser DNI o RUC
    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "DNI|RUC", message = "El tipo de documento solo puede ser DNI o RUC")
    @Column(name = "document_type", nullable = false)
    private String documentType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
