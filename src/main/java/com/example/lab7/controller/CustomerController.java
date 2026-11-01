package com.example.lab7.controller;

import com.example.lab7.entity.Customer;
import com.example.lab7.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * CRUD de clientes (tabla customer).
 * Rutas: /customer/list, /customer/new, /customer/edit?id=, /customer/save, /customer/delete?id=
 */
@Controller
@RequestMapping("/customer")
public class CustomerController {

    final CustomerRepository customerRepository;

    // Inyeccion de dependencias por constructor
    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // ---------- LISTAR ----------
    @GetMapping({"", "/", "/list"})
    public String listar(Model model) {
        model.addAttribute("customerList", customerRepository.findAll());
        return "customer/list";
    }

    // ---------- NUEVO ----------
    // @ModelAttribute crea un Customer vacio ("la cajita vacia") que se envia al formulario.
    @GetMapping("/new")
    public String nuevo(@ModelAttribute("customer") Customer customer) {
        return "customer/form";
    }

    // ---------- EDITAR ----------
    // Se reutiliza la MISMA vista customer/form, pero ahora con el cliente lleno.
    @GetMapping("/edit")
    public String editar(@ModelAttribute("customer") Customer customer,
                         @RequestParam("id") Integer id,
                         Model model, RedirectAttributes attr) {
        Optional<Customer> optCustomer = customerRepository.findById(id);
        if (optCustomer.isPresent()) {
            // Reemplaza el objeto vacio del @ModelAttribute por el cliente de la BD
            model.addAttribute("customer", optCustomer.get());
            return "customer/form";
        }
        attr.addFlashAttribute("error", "El cliente no existe");
        return "redirect:/customer/list";
    }

    // ---------- GUARDAR (crear y actualizar) ----------
    // @Valid ejecuta las validaciones de la entidad y BindingResult (justo despues) guarda los errores.
    @PostMapping("/save")
    public String guardar(@ModelAttribute("customer") @Valid Customer customer,
                          BindingResult bindingResult,
                          RedirectAttributes attr) {

        // 1) Regla que depende de dos campos: longitud segun el tipo de documento
        String doc = customer.getDocument();
        String tipo = customer.getDocumentType();
        if (doc != null && !doc.isBlank() && tipo != null) {
            if ("DNI".equals(tipo) && !doc.matches("\\d{8}")) {
                bindingResult.rejectValue("document", "document.dni",
                        "El DNI debe contener exactamente 8 dígitos numéricos");
            } else if ("RUC".equals(tipo) && !doc.matches("\\d{11}")) {
                bindingResult.rejectValue("document", "document.ruc",
                        "El RUC debe contener exactamente 11 dígitos numéricos");
            }
        }

        // 2) Documento unico: se consulta ANTES de guardar para dar un mensaje comprensible
        if (doc != null && !doc.isBlank()) {
            boolean duplicado = (customer.getId() == null)
                    ? customerRepository.existsByDocument(doc)
                    : customerRepository.existsByDocumentAndIdNot(doc, customer.getId());
            if (duplicado) {
                bindingResult.rejectValue("document", "document.duplicado",
                        "Ya existe un cliente registrado con el documento " + doc);
            }
        }

        // 3) Si hay errores se vuelve al formulario: gracias a @ModelAttribute
        //    los valores ingresados y los errores por campo se conservan solos.
        if (bindingResult.hasErrors()) {
            return "customer/form";
        }

        // Mensaje distinto si es nuevo o edicion (se valida ANTES del save, que asigna el id)
        String msg = (customer.getId() == null) ? "Cliente registrado exitosamente"
                                                : "Cliente actualizado exitosamente";
        try {
            customerRepository.save(customer);
            attr.addFlashAttribute("msg", msg);
        } catch (DataIntegrityViolationException e) {
            // Respaldo por si la restriccion UNIQUE de la BD salta igual (p. ej. dos usuarios a la vez)
            attr.addFlashAttribute("error", "No se pudo guardar: el documento ya está registrado");
        }
        return "redirect:/customer/list";
    }

    // ---------- BORRAR ----------
    @GetMapping("/delete")
    public String borrar(@RequestParam("id") Integer id, RedirectAttributes attr) {
        if (customerRepository.existsById(id)) {
            try {
                customerRepository.deleteById(id);
                attr.addFlashAttribute("msg", "Cliente eliminado exitosamente");
            } catch (DataIntegrityViolationException e) {
                // FK invoice.customer_id ON DELETE RESTRICT
                attr.addFlashAttribute("error", "No se puede eliminar el cliente porque tiene pedidos registrados");
            }
        }
        return "redirect:/customer/list";
    }
}
