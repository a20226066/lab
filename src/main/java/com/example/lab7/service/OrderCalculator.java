package com.example.lab7.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculos del pedido en Java (el laboratorio prohibe JavaScript).
 * Se usa BigDecimal para evitar errores de redondeo de double (p. ej. 3 x 0.1 = 0.30000000000000004)
 * y se redondea a 2 decimales (centimos).
 */
public final class OrderCalculator {

    private OrderCalculator() {
    }

    // subtotal = cantidad x precio
    public static double subtotal(int quantity, double price) {
        return BigDecimal.valueOf(price)
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    // total = total + subtotal
    public static double sumar(double total, double subtotal) {
        return BigDecimal.valueOf(total)
                .add(BigDecimal.valueOf(subtotal))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
