package com.example.lab7.controller;

import com.example.lab7.entity.Product;
import com.example.lab7.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * CRUD del catalogo de productos (tabla product).
 *
 * GESTION DE CONVERSION (letras en campos numericos):
 *  -> Se implemento la alternativa "messages.properties" (Camino 1).
 *     Si el usuario escribe letras en precio o stock, Spring registra un error
 *     "typeMismatch" en el BindingResult (no lanza excepcion), el campo queda en null,
 *     se muestra el mensaje personalizado de messages.properties y NO se guarda el producto.
 */
@Controller
@RequestMapping("/product")
public class ProductController {

    final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ---------- LISTAR ----------
    @GetMapping({"", "/", "/list"})
    public String listar(Model model) {
        model.addAttribute("productList", productRepository.findAll());
        return "product/list";
    }

    // ---------- NUEVO ----------
    @GetMapping("/new")
    public String nuevo(@ModelAttribute("product") Product product) {
        return "product/form";
    }

    // ---------- EDITAR (misma vista que nuevo) ----------
    @GetMapping("/edit")
    public String editar(@ModelAttribute("product") Product product,
                         @RequestParam("id") Integer id,
                         Model model, RedirectAttributes attr) {
        Optional<Product> optProduct = productRepository.findById(id);
        if (optProduct.isPresent()) {
            model.addAttribute("product", optProduct.get());
            return "product/form";
        }
        attr.addFlashAttribute("error", "El producto no existe");
        return "redirect:/product/list";
    }

    // ---------- GUARDAR (crear y actualizar) ----------
    @PostMapping("/save")
    public String guardar(@ModelAttribute("product") @Valid Product product,
                          BindingResult bindingResult,
                          RedirectAttributes attr) {

        // Nombre unico (no se puede expresar con una anotacion: requiere consultar la BD)
        String nombre = product.getName();
        if (nombre != null && !nombre.isBlank()) {
            product.setName(nombre.trim());
            boolean duplicado = (product.getId() == null)
                    ? productRepository.existsByNameIgnoreCase(product.getName())
                    : productRepository.existsByNameIgnoreCaseAndIdNot(product.getName(), product.getId());
            if (duplicado) {
                bindingResult.rejectValue("name", "name.duplicado",
                        "Ya existe un producto con el nombre \"" + product.getName() + "\"");
            }
        }

        // Si hay errores de validacion (@Valid) o de conversion (typeMismatch) NO se guarda;
        // se regresa al formulario con los datos ingresados y los mensajes por campo.
        if (bindingResult.hasErrors()) {
            return "product/form";
        }

        String msg = (product.getId() == null) ? "Producto registrado exitosamente"
                                               : "Producto actualizado exitosamente";
        try {
            productRepository.save(product);
            attr.addFlashAttribute("msg", msg);
        } catch (DataIntegrityViolationException e) {
            attr.addFlashAttribute("error", "No se pudo guardar: el nombre del producto ya existe");
        }
        return "redirect:/product/list";
    }

    // ---------- BORRAR ----------
    @GetMapping("/delete")
    public String borrar(@RequestParam("id") Integer id, RedirectAttributes attr) {
        if (productRepository.existsById(id)) {
            try {
                productRepository.deleteById(id);
                attr.addFlashAttribute("msg", "Producto eliminado exitosamente");
            } catch (DataIntegrityViolationException e) {
                // FK invoice_detail.product_id ON DELETE RESTRICT
                attr.addFlashAttribute("error", "No se puede eliminar el producto porque figura en pedidos registrados");
            }
        }
        return "redirect:/product/list";
    }
}
