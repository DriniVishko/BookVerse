
package com.bookverse;

import com.bookverse.database.DatabaseInitializer;
import com.bookverse.database.DatabaseSeeder;
import com.bookverse.model.LineaPedido;
import com.bookverse.service.PedidoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest {

    private Connection conexion;
    private PedidoService servicio;

    @BeforeEach
    void prepararBaseDeDatos() throws SQLException {
        conexion = DriverManager.getConnection(
                "jdbc:h2:mem:test_" + System.nanoTime()
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        DatabaseInitializer.crearTablaLibros(conexion);
        DatabaseInitializer.crearTablaAutor(conexion);
        DatabaseInitializer.crearTablaLibroAutor(conexion);
        DatabaseInitializer.crearTablaCliente(conexion);
        DatabaseInitializer.crearTablaInventario(conexion);
        DatabaseInitializer.crearTablaMovimientoStock(conexion);
        DatabaseInitializer.crearTablaPedido(conexion);
        DatabaseInitializer.crearTablaDetallePedido(conexion);
        DatabaseInitializer.crearTablaPago(conexion);

        DatabaseSeeder.insertarDatosIniciales(conexion);
        servicio = new PedidoService();
    }

    @AfterEach
    void cerrarConexion() throws SQLException {
        if (conexion != null) {
            conexion.close();
        }
    }

    @Test
    void crearPedidoCorrectamente() throws SQLException {
        int idPedido = servicio.realizarPedido(
                conexion, 1, 1, 2, false
        );

        assertTrue(idPedido > 0);
        assertEquals("PAGADO", consultarEstadoPedido(idPedido));
    }

    @Test
    void comprarDescuentaStock() throws SQLException {
        int stockInicial = consultarStock(1);

        servicio.realizarPedido(conexion, 1, 1, 2, false);

        assertEquals(stockInicial - 2, consultarStock(1));
    }

    @Test
    void cancelarPedidoDevuelveStock() throws SQLException {
        int stockInicial = consultarStock(1);

        int idPedido = servicio.realizarPedido(
                conexion, 1, 1, 2, false
        );

        servicio.cancelarPedido(conexion, idPedido);

        assertEquals(stockInicial, consultarStock(1));
        assertEquals("CANCELADO", consultarEstadoPedido(idPedido));
    }

    @Test
    void noPermiteComprarSinStock() throws SQLException {
        int stockInicial = consultarStock(1);

        assertThrows(SQLException.class, () ->
                servicio.realizarPedido(
                        conexion, 1, 1, stockInicial + 100, false
                )
        );

        assertEquals(stockInicial, consultarStock(1));
    }

    @Test
    void rollbackRecuperaStock() throws SQLException {
        int stockInicial = consultarStock(1);

        assertThrows(SQLException.class, () ->
                servicio.realizarPedido(conexion, 1, 1, 3, true)
        );

        assertEquals(stockInicial, consultarStock(1));
        assertEquals(0, contarFilas("pedido"));
        assertEquals(0, contarFilas("detalle_pedido"));
        assertEquals(0, contarFilas("pago"));
        assertEquals(0, contarFilas("movimiento_stock"));
    }

    @Test
    void cancelarDosVecesNoDuplicaStock() throws SQLException {
        int stockInicial = consultarStock(1);

        int idPedido = servicio.realizarPedido(
                conexion,
                1,
                List.of(
                        new LineaPedido(1, 2),
                        new LineaPedido(2, 1)
                ),
                false
        );

        servicio.cancelarPedido(conexion, idPedido);

        try {
            servicio.cancelarPedido(conexion, idPedido);
        } catch (SQLException | IllegalStateException ignored) {
            // También es válido que el servicio rechace
            // una segunda cancelación.
        }

        assertEquals(stockInicial, consultarStock(1));
        assertEquals(10, consultarStock(2));
        assertEquals("CANCELADO", consultarEstadoPedido(idPedido));
    }

    private int consultarStock(int idLibro) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT stock FROM inventario WHERE id_libro = ?"
        )) {
            ps.setInt(1, idLibro);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt("stock");
            }
        }
    }

    private String consultarEstadoPedido(int idPedido)
            throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT estado FROM pedido WHERE id_pedido = ?"
        )) {
            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getString("estado");
            }
        }
    }

    private int contarFilas(String tabla) throws SQLException {
        String sql = switch (tabla) {
            case "pedido", "detalle_pedido",
                 "pago", "movimiento_stock" ->
                    "SELECT COUNT(*) FROM " + tabla;
            default -> throw new IllegalArgumentException(
                    "Tabla no permitida: " + tabla
            );
        };

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}

