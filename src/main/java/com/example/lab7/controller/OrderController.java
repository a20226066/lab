package com.example.lab7.controller;

import com.example.lab7.dto.OrderForm;
import com.example.lab7.dto.OrderLineForm;
import com.example.lab7.entity.Customer;
import com.example.lab7.entity.Invoice;
import com.example.lab7.entity.Product;
import com.example.lab7.repository.CustomerRepository;
import com.example.lab7.repository.InvoiceRepository;
import com.example.lab7.repository.ProductRepository;
import com.example.lab7.service.OrderCalculator;
import com.example.lab7.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

/**
 * Registro y listado de pedidos (tablas invoice e invoice_detail).
 *
 * Como NO se permite JavaScript, el formulario usa varios botones submit que llegan
 * al mismo /order/save y se distinguen por el parametro "params" del @PostMapping:
 *   - addLine     -> agrega una fila de producto
 *   - removeLine  -> quita la fila indicada
 *   - calculate   -> valida y calcula subtotales/total en Java sin guardar
 *   - (ninguno)   -> valida y registra el pedido
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    final InvoiceRepository invoiceRepository;
    final CustomerRepository customerRepository;
    final ProductRepository productRepository;
    final OrderService orderService;

    public OrderController(InvoiceRepository invoiceRepository,
                           CustomerRepository customerRepository,
                           ProductRepository productRepository,
                           OrderService orderService) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderService = orderService;
    }

    // ===================== LISTAR =====================
    @GetMapping({"", "/", "/list"})
    public String listar(Model model) {
        // Mas recientes primero
        model.addAttribute("invoiceList", invoiceRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "order/list";
    }

    // ===================== VER DETALLE =====================
    @GetMapping("/view")
    public String ver(@RequestParam("id") Integer id, Model model, RedirectAttributes attr) {
        Optional<Invoice> optInvoice = invoiceRepository.findById(id);
        if (optInvoice.isEmpty()) {
            attr.addFlashAttribute("error", "El pedido no existe");
            return "redirect:/order/list";
        }
        model.addAttribute("invoice", optInvoice.get());
        return "order/view";
    }

    // ===================== NUEVO =====================
    @GetMapping("/new")
    public String nuevo(@ModelAttribute("orderForm") OrderForm orderForm, Model model) {
        orderForm.setDate(LocalDate.now());          // fecha sugerida: hoy
        orderForm.getLines().add(new OrderLineForm()); // una fila vacia para empezar
        cargarListas(model);
        return "order/form";
    }

    // ===================== BOTON "Agregar producto" =====================
    @PostMapping(value = "/save", params = "addLine")
    public String agregarLinea(@ModelAttribute("orderForm") OrderForm orderForm,
                               BindingResult bindingResult, Model model) {
        orderForm.getLines().add(new OrderLineForm());
        return recargarSinErrores(orderForm, model);
    }

    // ===================== BOTON "Quitar" =====================
    @PostMapping(value = "/save", params = "removeLine")
    public String quitarLinea(@ModelAttribute("orderForm") OrderForm orderForm,
                              BindingResult bindingResult,
                              @RequestParam("removeLine") int index, Model model) {
        if (index >= 0 && index < orderForm.getLines().size()) {
            orderForm.getLines().remove(index);
        }
        return recargarSinErrores(orderForm, model);
    }

    // ===================== BOTON "Calcular total" =====================
    @PostMapping(value = "/save", params = "calculate")
    public String calcular(@ModelAttribute("orderForm") @Valid OrderForm orderForm,
                           BindingResult bindingResult, Model model) {
        validarReglasDeNegocio(orderForm, bindingResult);
        if (!bindingResult.hasErrors()) {
            model.addAttribute("info", "Totales calculados. Revise el pedido y presione \"Registrar pedido\".");
        }
        cargarListas(model);
        return "order/form";
    }

    // ===================== REGISTRAR PEDIDO =====================
    @PostMapping("/save")
    public String guardar(@ModelAttribute("orderForm") @Valid OrderForm orderForm,
                          BindingResult bindingResult,
                          Model model, RedirectAttributes attr) {

        // Reglas que no se pueden expresar con anotaciones (dependen de la BD o de varios campos)
        Customer customer = validarReglasDeNegocio(orderForm, bindingResult);

        // Errores de validacion o de binding -> NO se registra nada y se vuelve al formulario.
        // Gracias a @ModelAttribute se conserva el cliente, tipo, fecha y productos elegidos.
        if (bindingResult.hasErrors()) {
            cargarListas(model);
            return "order/form";
        }

        try {
            Invoice invoice = orderService.registrar(orderForm, customer);
            attr.addFlashAttribute("msg", "Pedido N° " + invoice.getId() + " registrado exitosamente. Total: S/ "
                    + String.format(Locale.US, "%.2f", orderForm.getTotal()));
            return "redirect:/order/list";
        } catch (OrderService.OrderException e) {
            // Rollback ya realizado por @Transactional: se muestra como error global
            bindingResult.reject("order.error", e.getMessage());
            cargarListas(model);
            return "order/form";
        }
    }

    /**
     * Valida las reglas del pedido y, de paso, calcula subtotales y total EN JAVA.
     * Errores de un campo   -> bindingResult.rejectValue(campo, ...)
     * Errores de varios campos -> bindingResult.reject(...)  (mensaje global)
     *
     * @return el cliente seleccionado (o null si no es valido)
     */
    private Customer validarReglasDeNegocio(OrderForm form, BindingResult br) {

        // ---------- Cliente ----------
        Customer customer = null;
        if (form.getCustomerId() != null) {
            customer = customerRepository.findById(form.getCustomerId()).orElse(null);
            if (customer == null) {
                br.rejectValue("customerId", "customer.noExiste", "El cliente seleccionado no existe");
            }
        }

        // ---------- Regla de varios campos: tipo de comprobante vs documento del cliente ----------
        if (customer != null && form.getType() != null) {
            if ("Factura".equals(form.getType()) && !"RUC".equals(customer.getDocumentType())) {
                br.reject("tipo.incompatible",
                        "Una Factura solo puede emitirse a un cliente con RUC. El cliente "
                                + customer.getName() + " tiene " + customer.getDocumentType() + ".");
            } else if ("Boleta".equals(form.getType()) && !"DNI".equals(customer.getDocumentType())) {
                br.reject("tipo.incompatible",
                        "Una Boleta solo puede emitirse a un cliente con DNI. El cliente "
                                + customer.getName() + " tiene " + customer.getDocumentType() + ".");
            }
        }

        // ---------- Lineas de producto ----------
        Set<Integer> productosUsados = new HashSet<>();
        boolean hayRepetidos = false;
        double total = 0;

        for (int i = 0; i < form.getLines().size(); i++) {
            OrderLineForm line = form.getLines().get(i);
            line.setPrice(null);
            line.setSubtotal(null);

            if (line.getProductId() == null) {
                continue; // @NotNull ya registro el error "Seleccione un producto"
            }

            Product product = productRepository.findById(line.getProductId()).orElse(null);
            if (product == null) {
                br.rejectValue("lines[" + i + "].productId", "product.noExiste", "El producto no existe");
                continue;
            }

            // No repetir el mismo producto dentro del pedido
            if (!productosUsados.add(product.getId())) {
                hayRepetidos = true;
                br.rejectValue("lines[" + i + "].productId", "product.repetido",
                        "El producto " + product.getName() + " ya fue agregado en otra fila");
            }

            // Cantidad: solo se revisa el stock si paso las validaciones basicas (no null y > 0)
            Integer q = line.getQuantity();
            if (q != null && q > 0) {
                if (q > product.getStock()) {
                    br.rejectValue("lines[" + i + "].quantity", "quantity.stock",
                            "Supera el stock disponible (" + product.getStock() + ")");
                }
                // Calculo en Java: subtotal = cantidad x precio
                line.setPrice(product.getPrice());
                line.setSubtotal(OrderCalculator.subtotal(q, product.getPrice()));
                total = OrderCalculator.sumar(total, line.getSubtotal());
            }
        }

        if (hayRepetidos) {
            br.reject("product.repetido", "No se puede repetir el mismo producto dentro de un pedido.");
        }

        form.setTotal(total);
        return customer;
    }

    /**
     * Al agregar/quitar filas no se muestran errores (el usuario aun esta llenando el formulario).
     * Se elimina el BindingResult del modelo para que Thymeleaf lea los valores del objeto
     * (los indices de las filas pudieron cambiar). Los valores no convertibles se pierden (quedan vacios).
     */
    private String recargarSinErrores(OrderForm orderForm, Model model) {
        model.asMap().remove(BindingResult.MODEL_KEY_PREFIX + "orderForm");
        // Se recalculan subtotales para lo que ya se haya llenado correctamente
        for (OrderLineForm line : orderForm.getLines()) {
            line.setPrice(null);
            line.setSubtotal(null);
        }
        double total = 0;
        for (OrderLineForm line : orderForm.getLines()) {
            if (line.getProductId() != null && line.getQuantity() != null && line.getQuantity() > 0) {
                Optional<Product> p = productRepository.findById(line.getProductId());
                if (p.isPresent()) {
                    line.setPrice(p.get().getPrice());
                    line.setSubtotal(OrderCalculator.subtotal(line.getQuantity(), p.get().getPrice()));
                    total = OrderCalculator.sumar(total, line.getSubtotal());
                }
            }
        }
        orderForm.setTotal(total);
        cargarListas(model);
        return "order/form";
    }

    // Listas para los <select> del formulario (se usa Model porque no hay redirect)
    private void cargarListas(Model model) {
        model.addAttribute("customerList", customerRepository.findAll(Sort.by("name")));
        model.addAttribute("productList", productRepository.findAll(Sort.by("name")));
        model.addAttribute("today", LocalDate.now());
    }

    /**
     * @InitBinder: precio, subtotal y total NO se aceptan desde el formulario
     * (un usuario podria enviarlos alterados). Siempre se calculan en Java.
     * Nota: la conversion de letras en numeros se maneja con messages.properties.
     */
    @InitBinder("orderForm")
    public void protegerCamposCalculados(WebDataBinder binder) {
        binder.setDisallowedFields("total", "lines*.price", "lines*.subtotal");
    }
}
