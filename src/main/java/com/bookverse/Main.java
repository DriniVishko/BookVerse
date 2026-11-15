
package com.bookverse;

import com.bookverse.database.DatabaseInitializer;
import com.bookverse.database.DatabaseSeeder;
import com.bookverse.service.PedidoService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/unidad2";
        String usuario = "root";
        String password = System.getenv("BOOKVERSE_DB_PASSWORD");

        try (Connection conexion =
                     DriverManager.getConnection(url, usuario, password)) {

            System.out.println("Conexión correcta con MySQL.");

            // Reiniciar la base de datos
            DatabaseInitializer.eliminarTablas(conexion);

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

            PedidoService pedidoService = new PedidoService();

            // PRUEBA 1: compra correcta
            int stockInicial = consultarStock(conexion, 1);

            int idPedido = pedidoService.realizarPedido(
                    conexion,
                    1,
                    java.util.List.of(
                            new com.bookverse.model.LineaPedido(1, 2),
                            new com.bookverse.model.LineaPedido(2, 1)
                    ),
                    false
            );

            System.out.println("Pedido creado: " + idPedido);
            System.out.println("Stock inicial: " + stockInicial);
            System.out.println(
                    "Stock después de comprar: "
                            + consultarStock(conexion, 1)
            );

            // PRUEBA 2: cancelación
            pedidoService.cancelarPedido(conexion, idPedido);

            System.out.println(
                    "Stock después de cancelar: "
                            + consultarStock(conexion, 1)
            );

            // PRUEBA 3: fallo intencionado
            int stockAntesFallo = consultarStock(conexion, 1);

            try {
                pedidoService.realizarPedido(
                        conexion, 1, 1, 3, true
                );
            } catch (SQLException e) {
                System.out.println(
                        "Fallo esperado: " + e.getMessage()
                );
            }

            int stockDespuesFallo = consultarStock(conexion, 1);

            System.out.println(
                    "Stock antes del fallo: " + stockAntesFallo
            );
            System.out.println(
                    "Stock después del rollback: " + stockDespuesFallo
            );

            if (stockAntesFallo == stockDespuesFallo) {
                System.out.println("ROLLBACK CORRECTO");
            } else {
                throw new IllegalStateException(
                        "ERROR: el rollback no restauró el stock"
                );
            }

            System.out.println("PRUEBAS FINALIZADAS");

        } catch (SQLException e) {
            System.err.println("Error SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int consultarStock(
            Connection conexion, int idLibro
    ) throws SQLException {

        String sql = """
                SELECT stock
                FROM inventario
                WHERE id_libro = ?
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idLibro);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "No existe inventario para el libro " + idLibro
                    );
                }

                return rs.getInt("stock");
            }
        }
    }
}

