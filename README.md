# Laboratorio 6 – Control de inventario y pedidos (TEL137)

Spring Boot 3 + Spring MVC + Thymeleaf + Spring Data JPA + Hibernate Validator. Sin JavaScript.

## Ejecutar
1. Crear la BD con `db/lab6.sql` en MySQL.
2. Ajustar usuario/contraseña en `src/main/resources/application.properties`.
3. `./mvnw spring-boot:run` (o ejecutar `Lab7Application` desde IntelliJ) y abrir http://localhost:8080

## Estructura
| Paquete / carpeta | Contenido |
|---|---|
| `controller` | `HomeController`, `CustomerController`, `ProductController`, `OrderController` |
| `entity` | `Customer`, `Product`, `Invoice`, `InvoiceDetail` (tablas sin cambios) |
| `repository` | Repositorios `JpaRepository` con query methods |
| `dto` | `OrderForm`, `OrderLineForm` (objeto del formulario de pedido) |
| `service` | `OrderService` (registro transaccional) y `OrderCalculator` (subtotal/total) |
| `templates/fragments/menu.html` | Fragmentos reutilizables: `head`, `menu`, `alertas` |
| `messages.properties` | Mensajes personalizados de conversión (`typeMismatch`) |

## Decisiones
- **Conversión de letras en números:** se implementó la alternativa **messages.properties** (comentado en `ProductController` y en el propio archivo).
- **Formulario de pedido sin JS:** varios botones *submit* al mismo `/order/save`, diferenciados con `@PostMapping(params = ...)` (agregar fila, quitar fila, calcular, registrar).
- **Reglas de varios campos** (Factura↔RUC, Boleta↔DNI, productos repetidos): `bindingResult.reject(...)` → mensaje global.
- **Precio, subtotal y total** se calculan en Java y se bloquean del databinding con `@InitBinder`.
- Al registrar un pedido se descuenta el stock; todo en una transacción (`@Transactional`).

## Ramas
`main` (vacía) → `develop` → `feature/customer` → `feature/product` → merge a `develop`.
