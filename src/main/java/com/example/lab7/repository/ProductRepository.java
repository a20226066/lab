package com.example.lab7.repository;

import com.example.lab7.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Nombre unico sin distinguir mayusculas/minusculas ("Arroz" = "arroz")
    boolean existsByNameIgnoreCase(String name);

    // En edicion: ¿otro producto (id distinto) ya usa ese nombre?
    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
