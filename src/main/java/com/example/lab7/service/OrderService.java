package com.example.lab7.service;

import com.example.lab7.dto.OrderForm;
import com.example.lab7.dto.OrderLineForm;
import com.example.lab7.entity.Customer;
import com.example.lab7.entity.Invoice;
import com.example.lab7.entity.InvoiceDetail;
import com.example.lab7.entity.Product;
import com.example.lab7.repository.InvoiceDetailRepository;
import com.example.lab7.repository.InvoiceRepository;
import com.example.lab7.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra un pedido (cabecera + detalles) en UNA sola transaccion.
 * Si algo falla a mitad (p. ej. el stock cambio mientras se llenaba el formulario),
 * @Transactional hace rollback: no queda ni la cabecera ni ningun detalle guardado.
 */
@Service
public class OrderService {

    final InvoiceRepository invoiceRepository;
    final InvoiceDetailRepository invoiceDetailRepository;
    final ProductRepository productRepository;

    public OrderService(InvoiceRepository invoiceRepository,
                        InvoiceDetailRepository invoiceDetailRepository,
                        ProductRepository productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceDetailRepository = invoiceDetailRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Invoice registrar(OrderForm form, Customer customer) {
        // 1) Cabecera (invoice)
        Invoice invoice = new Invoice();
        invoice.setType(form.getType());
        invoice.setDate(form.getDate());
        invoice.setCustomer(customer);
        invoiceRepository.save(invoice);

        // 2) Detalles (invoice_detail)
        for (OrderLineForm line : form.getLines()) {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new OrderException("Uno de los productos ya no existe"));

            // Se vuelve a verificar el stock dentro de la transaccion
            if (line.getQuantity() > product.getStock()) {
                throw new OrderException("Stock insuficiente para " + product.getName()
                        + " (disponible: " + product.getStock() + ")");
            }

            InvoiceDetail detail = new InvoiceDetail();
            detail.setInvoice(invoice);
            detail.setProduct(product);
            detail.setQuantity(line.getQuantity());
            detail.setPrice(product.getPrice());                      // precio actual del catalogo
            detail.setSubtotal(OrderCalculator.subtotal(line.getQuantity(), product.getPrice()));
            invoiceDetailRepository.save(detail);

            // 3) Se descuenta el stock vendido
            product.setStock(product.getStock() - line.getQuantity());
            productRepository.save(product);
        }
        return invoice;
    }

    /** Excepcion de negocio: al ser RuntimeException provoca el rollback de @Transactional. */
    public static class OrderException extends RuntimeException {
        public OrderException(String message) {
            super(message);
        }
    }
}
