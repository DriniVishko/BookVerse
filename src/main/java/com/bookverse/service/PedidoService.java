
package com.bookverse.service;

import com.bookverse.model.LineaPedido;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PedidoService {

    public int realizarPedido(
            Connection conexion,
            int idCliente,
            int idLibro,
            int cantidad,
            boolean simularFallo
    ) throws SQLException {
        return realizarPedido(
                conexion,
                idCliente,
                List.of(new LineaPedido(idLibro, cantidad)),
                simularFallo
        );
    }

    public int realizarPedido(
            Connection conexion,
            int idCliente,
            List<LineaPedido> lineas,
            boolean simularFallo
    ) throws SQLException {

        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener libros");
        }

        Set<Integer> libros = new HashSet<>();

        for (LineaPedido linea : lineas) {
            if (linea.idLibro() <= 0 || linea.cantidad() <= 0) {
                throw new IllegalArgumentException("Libro o cantidad no válidos");
            }
            if (!libros.add(linea.idLibro())) {
                throw new IllegalArgumentException(
                        "No se puede repetir un libro en el mismo pedido"
                );
            }
        }

        boolean autoCommitOriginal = conexion.getAutoCommit();

        try {
            conexion.setAutoCommit(false);

            BigDecimal total = BigDecimal.ZERO;

            // Calcular el total antes de crear el pedido
            for (LineaPedido linea : lineas) {
                BigDecimal precio = consultarPrecio(conexion, linea.idLibro());
                total = total.add(
                        precio.multiply(BigDecimal.valueOf(linea.cantidad()))
                );
            }

            int idPedido = crearPedido(conexion, idCliente, total);

            for (LineaPedido linea : lineas) {
                BigDecimal precio = consultarPrecio(conexion, linea.idLibro());

                insertarDetalle(
                        conexion, idPedido, linea.idLibro(),
                        linea.cantidad(), precio
                );

                descontarStock(
                        conexion, linea.idLibro(), linea.cantidad()
                );

                registrarMovimiento(
                        conexion, linea.idLibro(),
                        "SALIDA", linea.cantidad()
                );
            }

            if (simularFallo) {
                throw new SQLException(
                        "Fallo de transacción simulado intencionadamente"
                );
            }

            registrarPago(conexion, idPedido, total);

            try (PreparedStatement ps = conexion.prepareStatement("""
                    UPDATE pedido
                    SET estado = 'PAGADO'
                    WHERE id_pedido = ?
                    """)) {
                ps.setInt(1, idPedido);
                ps.executeUpdate();
            }

            conexion.commit();
            return idPedido;

        } catch (SQLException | RuntimeException e) {
            try {
                conexion.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;

        } finally {
            conexion.setAutoCommit(autoCommitOriginal);
        }
    }

    private BigDecimal consultarPrecio(
            Connection conexion, int idLibro
    ) throws SQLException {

        try (PreparedStatement ps = conexion.prepareStatement("""
                SELECT precio FROM libro WHERE id_libro = ?
                """)) {

            ps.setInt(1, idLibro);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Libro inexistente: " + idLibro);
                }
                return rs.getBigDecimal("precio");
            }
        }
    }

    private int crearPedido(
            Connection conexion, int idCliente, BigDecimal total
    ) throws SQLException {

        try (PreparedStatement ps = conexion.prepareStatement("""
                INSERT INTO pedido (id_cliente, estado, total)
                VALUES (?, 'PENDIENTE', ?)
                """, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idCliente);
            ps.setBigDecimal(2, total);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se generó el ID del pedido");
                }
                return rs.getInt(1);
            }
        }
    }

    private void insertarDetalle(
            Connection conexion, int idPedido, int idLibro,
            int cantidad, BigDecimal precio
    ) throws SQLException {

        try (PreparedStatement ps = conexion.prepareStatement("""
                INSERT INTO detalle_pedido
                (id_pedido, id_libro, cantidad, precio_unitario)
                VALUES (?, ?, ?, ?)
                """)) {

            ps.setInt(1, idPedido);
            ps.setInt(2, idLibro);
            ps.setInt(3, cantidad);
            ps.setBigDecimal(4, precio);
            ps.executeUpdate();
        }
    }

    private void descontarStock(
            Connection conexion, int idLibro, int cantidad
    ) throws SQLException {

        try (PreparedStatement ps = conexion.prepareStatement("""
                UPDATE inventario
                SET stock = stock - ?
                WHERE id_libro = ? AND stock >= ?
                """)) {

            ps.setInt(1, cantidad);
            ps.setInt(2, idLibro);
            ps.setInt(3, cantidad);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "Stock insuficiente para el libro " + idLibro
                );
            }
        }
    }

    private void registrarMovimiento(
            Connection conexion, int idLibro,
            String tipo, int cantidad
    ) throws SQLException {

        try (PreparedStatement ps = conexion.prepareStatement("""
                INSERT INTO movimiento_stock
                (id_libro, tipo, cantidad)
                VALUES (?, ?, ?)
                """)) {

            ps.setInt(1, idLibro);
            ps.setString(2, tipo);
            ps.setInt(3, cantidad);
            ps.executeUpdate();
        }
    }

    private void registrarPago(
            Connection conexion, int idPedido, BigDecimal total
    ) throws SQLException {

        try (PreparedStatement ps = conexion.prepareStatement("""
                INSERT INTO pago
                (id_pedido, metodo, estado, importe)
                VALUES (?, 'TARJETA', 'COMPLETADO', ?)
                """)) {

            ps.setInt(1, idPedido);
            ps.setBigDecimal(2, total);
            ps.executeUpdate();
        }
    }

    public void cancelarPedido(
            Connection conexion, int idPedido
    ) throws SQLException {

        boolean autoCommitOriginal = conexion.getAutoCommit();

        try {
            conexion.setAutoCommit(false);

            try (PreparedStatement ps = conexion.prepareStatement("""
                    UPDATE pedido
                    SET estado = 'CANCELADO'
                    WHERE id_pedido = ? AND estado = 'PAGADO'
                    """)) {

                ps.setInt(1, idPedido);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                            "Pedido inexistente, no pagado o ya cancelado"
                    );
                }
            }

            try (PreparedStatement ps = conexion.prepareStatement("""
                    SELECT id_libro, cantidad
                    FROM detalle_pedido
                    WHERE id_pedido = ?
                    """)) {

                ps.setInt(1, idPedido);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idLibro = rs.getInt("id_libro");
                        int cantidad = rs.getInt("cantidad");

                        try (PreparedStatement actualizar =
                                     conexion.prepareStatement("""
                                             UPDATE inventario
                                             SET stock = stock + ?
                                             WHERE id_libro = ?
                                             """)) {

                            actualizar.setInt(1, cantidad);
                            actualizar.setInt(2, idLibro);

                            if (actualizar.executeUpdate() != 1) {
                                throw new SQLException(
                                        "No se pudo restaurar el stock"
                                );
                            }
                        }

                        registrarMovimiento(
                                conexion, idLibro, "DEVOLUCION", cantidad
                        );
                    }
                }
            }

            conexion.commit();

        } catch (SQLException | RuntimeException e) {
            try {
                conexion.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;

        } finally {
            conexion.setAutoCommit(autoCommitOriginal);
        }
    }
}
