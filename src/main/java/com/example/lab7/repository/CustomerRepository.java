package com.example.lab7.repository;

import com.example.lab7.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // Query methods: Spring genera el SQL a partir del nombre del metodo

    // Al CREAR: ¿ya existe un cliente con ese documento?
    boolean existsByDocument(String document);

    // Al EDITAR: ¿existe OTRO cliente (id distinto) con ese documento?
    boolean existsByDocumentAndIdNot(String document, Integer id);
}
