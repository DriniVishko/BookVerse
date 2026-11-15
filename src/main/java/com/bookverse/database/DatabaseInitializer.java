package com.bookverse.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void crearTablaLibros(Connection conexion) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS libro (
                    id_libro INT AUTO_INCREMENT PRIMARY KEY,
                    titulo VARCHAR(200) NOT NULL,
                    isbn VARCHAR(13) NOT NULL UNIQUE,
                    precio DECIMAL(10,2) NOT NULL,
                    formato VARCHAR(30) NOT NULL,
                    editorial VARCHAR(120) NOT NULL
                )
                """;
        try (Statement statement = conexion.createStatement()) {
            statement.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("Error al crear la tabla 'libro': " + e.getMessage());
        }
    }

    public static void crearTablaAutor(Connection conexion) throws SQLException{
        String sql= """
            CREATE TABLE IF NOT EXISTS autor (
                id_autor INT AUTO_INCREMENT PRIMARY KEY,
                nombre VARCHAR(150) NOT NULL
            )
            """;
        try(Statement st = conexion.createStatement()){
            st.executeUpdate(sql);
        }
    }

    public static void crearTablaLibroAutor(Connection conexion) throws SQLException{
        String sql= """
            CREATE TABLE IF NOT EXISTS libro_autor (
                id_libro INT NOT NULL,
                id_autor INT NOT NULL,

                PRIMARY KEY (id_libro, id_autor),

                FOREIGN KEY (id_libro)
                    REFERENCES libro(id_libro),

                FOREIGN KEY (id_autor)
                    REFERENCES autor(id_autor)
            )
            """;
        try(Statement st = conexion.createStatement()){
            st.executeUpdate(sql);
        }
    }



    public static void crearTablaCliente(Connection conexion) throws SQLException{
        String sql= """
            CREATE TABLE IF NOT EXISTS cliente (
                id_cliente INT AUTO_INCREMENT PRIMARY KEY,
                nombre VARCHAR(100) NOT NULL,
                email VARCHAR(150) NOT NULL UNIQUE,
                telefono VARCHAR(20),
                direccion VARCHAR(200) NOT NULL,
                ciudad VARCHAR(100) NOT NULL,
                codigo_postal VARCHAR(10) NOT NULL
            )
            """;
        try(Statement st = conexion.createStatement()){
            st.executeUpdate(sql);
        }
    }

    public static void crearTablaInventario(Connection conexion) throws SQLException{
        String sql= """
            CREATE TABLE IF NOT EXISTS inventario (
                id_libro INT PRIMARY KEY,
                stock INT NOT NULL DEFAULT 0,
                stock_minimo INT NOT NULL DEFAULT 5,

                CONSTRAINT fk_inventario_libro
                    FOREIGN KEY (id_libro)
                    REFERENCES libro(id_libro),

                CONSTRAINT chk_stock
                    CHECK (stock >= 0),

                CONSTRAINT chk_stock_minimo
                    CHECK (stock_minimo >= 0)
            )
            """;
        try(Statement st = conexion.createStatement()){
            st.executeUpdate(sql);
        }
    }


    public static void crearTablaMovimientoStock(Connection conexion)
            throws SQLException {

        String sql = """
            CREATE TABLE IF NOT EXISTS movimiento_stock (
                id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
                id_libro INT NOT NULL,
                tipo VARCHAR(20) NOT NULL,
                cantidad INT NOT NULL,
                fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                CONSTRAINT fk_movimiento_inventario
                    FOREIGN KEY (id_libro)
                    REFERENCES inventario(id_libro),

                CONSTRAINT chk_tipo_movimiento
                    CHECK (tipo IN ('ENTRADA', 'SALIDA', 'DEVOLUCION')),

                CONSTRAINT chk_cantidad_movimiento
                    CHECK (cantidad > 0)
            )
            """;

        try (Statement statement = conexion.createStatement()) {
            statement.executeUpdate(sql);
        }
    }


    public static void crearTablaPedido(Connection conexion)
            throws SQLException {

        String sql = """
            CREATE TABLE IF NOT EXISTS pedido (
                id_pedido INT AUTO_INCREMENT PRIMARY KEY,
                id_cliente INT NOT NULL,
                fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
                total DECIMAL(10,2) NOT NULL DEFAULT 0.00,

                CONSTRAINT fk_pedido_cliente
                    FOREIGN KEY (id_cliente)
                    REFERENCES cliente(id_cliente),

                CONSTRAINT chk_estado_pedido
                    CHECK (estado IN ('PENDIENTE', 'PAGADO', 'CANCELADO')),

                CONSTRAINT chk_total_pedido
                    CHECK (total >= 0)
            )
            """;

        try (Statement statement = conexion.createStatement()) {
            statement.executeUpdate(sql);
        }
    }


    public static void crearTablaDetallePedido(Connection conexion)
            throws SQLException {

        String sql = """
            CREATE TABLE IF NOT EXISTS detalle_pedido (
                id_detalle INT AUTO_INCREMENT PRIMARY KEY,
                id_pedido INT NOT NULL,
                id_libro INT NOT NULL,
                cantidad INT NOT NULL,
                precio_unitario DECIMAL(10,2) NOT NULL,

                CONSTRAINT fk_detalle_pedido
                    FOREIGN KEY (id_pedido)
                    REFERENCES pedido(id_pedido),

                CONSTRAINT fk_detalle_libro
                    FOREIGN KEY (id_libro)
                    REFERENCES libro(id_libro),

                CONSTRAINT chk_detalle_cantidad
                    CHECK (cantidad > 0),

                CONSTRAINT chk_detalle_precio
                    CHECK (precio_unitario >= 0),

                CONSTRAINT uq_detalle_pedido_libro
                    UNIQUE (id_pedido, id_libro)
            )
            """;

        try (Statement statement = conexion.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public static void crearTablaPago(Connection conexion)
            throws SQLException {

        String sql = """
            CREATE TABLE IF NOT EXISTS pago (
                id_pago INT AUTO_INCREMENT PRIMARY KEY,
                id_pedido INT NOT NULL UNIQUE,
                metodo VARCHAR(20) NOT NULL,
                estado VARCHAR(20) NOT NULL,
                importe DECIMAL(10,2) NOT NULL,
                fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                CONSTRAINT fk_pago_pedido
                    FOREIGN KEY (id_pedido)
                    REFERENCES pedido(id_pedido),

                CONSTRAINT chk_metodo_pago
                    CHECK (metodo IN ('TARJETA', 'BIZUM', 'TRANSFERENCIA')),

                CONSTRAINT chk_estado_pago
                    CHECK (estado IN ('COMPLETADO', 'RECHAZADO')),

                CONSTRAINT chk_importe_pago
                    CHECK (importe >= 0)
            )
            """;

        try (Statement statement = conexion.createStatement()) {
            statement.executeUpdate(sql);
        }
    }


    public static void eliminarTablas(Connection conexion)
            throws SQLException {

        String[] tablas = {
                "pago",
                "detalle_pedido",
                "movimiento_stock",
                "pedido",
                "inventario",
                "libro_autor",
                "cliente",
                "autor",
                "libro"
        };

        try (Statement statement = conexion.createStatement()) {
            for (String tabla : tablas) {
                statement.executeUpdate(
                        "DROP TABLE IF EXISTS " + tabla
                );
            }
        }

        System.out.println("Tablas anteriores eliminadas.");
    }




}
