
# Diagrama entidad-relación de BookVerse

El modelo contiene nueve tablas y relaciones 1:1, 1:N y N:M.

```mermaid
erDiagram
    LIBRO ||--|| INVENTARIO : tiene
    LIBRO ||--o{ LIBRO_AUTOR : participa
    AUTOR ||--o{ LIBRO_AUTOR : escribe
    CLIENTE ||--o{ PEDIDO : realiza
    PEDIDO ||--|{ DETALLE_PEDIDO : contiene
    LIBRO ||--o{ DETALLE_PEDIDO : aparece
    PEDIDO ||--o| PAGO : recibe
    INVENTARIO ||--o{ MOVIMIENTO_STOCK : registra

    LIBRO {
        int id_libro PK
        string titulo
        string isbn UK
        decimal precio
        string formato
        string editorial
    }

    AUTOR {
        int id_autor PK
        string nombre
    }

    LIBRO_AUTOR {
        int id_libro PK,FK
        int id_autor PK,FK
    }

    CLIENTE {
        int id_cliente PK
        string nombre
        string email UK
        string telefono
        string direccion
        string ciudad
        string codigo_postal
    }

    INVENTARIO {
        int id_libro PK,FK
        int stock
        int stock_minimo
    }

    MOVIMIENTO_STOCK {
        int id_movimiento PK
        int id_libro FK
        string tipo
        int cantidad
        datetime fecha
    }

    PEDIDO {
        int id_pedido PK
        int id_cliente FK
        datetime fecha
        string estado
        decimal total
    }

    DETALLE_PEDIDO {
        int id_detalle PK
        int id_pedido FK
        int id_libro FK
        int cantidad
        decimal precio_unitario
    }

    PAGO {
        int id_pago PK
        int id_pedido FK,UK
        string metodo
        string estado
        decimal importe
        datetime fecha
    }
```

## Relaciones

- **1:1:** Libro–Inventario y Pedido–Pago.
- **1:N:** Cliente–Pedido, Pedido–Detalle y Libro–Movimientos.
- **N:M:** Libro–Autor mediante Libro_Autor.

El precio unitario se almacena en `detalle_pedido` para conservar el precio histórico de cada compra.
