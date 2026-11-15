
package com.bookverse;

import com.bookverse.database.*;
import com.bookverse.model.LineaPedido;
import com.bookverse.service.PedidoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.bookverse.database.ConsultasDao;
import com.bookverse.service.InventarioService;
import com.bookverse.database.ProcedimientosDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("INICIO DE BOOKVERSE");

        DatabaseConfig config = new DatabaseConfig();

        try (ConnectionFactory factory = new ConnectionFactory(config);
             Connection conexion = factory.getConnection()) {

            System.out.println(
                    "Motor: " + factory.getEngine()
                            + " | Conexión: " + factory.getMode()
            );

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

            ProcedimientosDao procedimientos = new ProcedimientosDao();

            if (factory.getEngine().equalsIgnoreCase("MYSQL")) {
                System.out.println("\n=== PROCEDIMIENTO ALMACENADO MYSQL ===");

                int stockProcedimiento =
                        procedimientos.consultarStockConProcedimiento(conexion, 1);

                System.out.println(
                        "Stock obtenido mediante CallableStatement: "
                                + stockProcedimiento
                );
            }

            System.out.println("\n=== ERROR SQL CONTROLADO ===");
            procedimientos.provocarErrorIntegridad(conexion);

            InventarioService inventarioService = new InventarioService();

            System.out.println("\n=== PRUEBA DE REPOSICIÓN ===");

            int stockAntesReposicion = consultarStock(conexion, 1);

            inventarioService.reponerStock(conexion, 1, 5);

            int stockDespuesReposicion = consultarStock(conexion, 1);

            System.out.println("Stock antes de reponer: " + stockAntesReposicion);
            System.out.println("Stock después de reponer: " + stockDespuesReposicion);

            ConsultasDao consultas = new ConsultasDao();

            System.out.println("\n=== PRUEBA DE CONSULTAS SQL ===");

            // JOIN: catálogo con autores y stock
            consultas.mostrarCatalogoConStock(conexion);

            // Paginación SQL
            System.out.println("\n=== CATÁLOGO PAGINADO ===");
            consultas.listarCatalogoPaginado(conexion, 1, 5)
                    .forEach(System.out::println);

            // Estadísticas con GROUP BY
            System.out.println("\n=== ESTADÍSTICAS DE VENTAS ===");
            consultas.obtenerEstadisticasVentas(conexion)
                    .forEach(System.out::println);

            // Inventario bajo mínimos
            consultas.mostrarLibrosBajoMinimo(conexion);

            PedidoService servicio = new PedidoService();

            int stockInicial = consultarStock(conexion, 1);

            int idPedido = servicio.realizarPedido(
                    conexion,
                    1,
                    List.of(
                            new LineaPedido(1, 2),
                            new LineaPedido(2, 1)
                    ),
                    false
            );

            System.out.println("Pedido creado: " + idPedido);
            System.out.println("Stock inicial IT: " + stockInicial);
            System.out.println(
                    "Stock después de comprar IT: "
                            + consultarStock(conexion, 1)
            );

            servicio.cancelarPedido(conexion, idPedido);

            System.out.println(
                    "Stock después de cancelar IT: "
                            + consultarStock(conexion, 1)
            );

            boolean simularFallo = config.get("simulation.mode")
                    .equalsIgnoreCase("FALLO_TRANSACCION");

            int stockAntes = consultarStock(conexion, 1);

            try {
                servicio.realizarPedido(
                        conexion, 1, 1, 3, simularFallo
                );
            } catch (SQLException e) {
                if (!simularFallo) {
                    throw e;
                }
                System.out.println("Fallo esperado: " + e.getMessage());
            }

            int stockDespues = consultarStock(conexion, 1);

            if (simularFallo) {
                if (stockAntes != stockDespues) {
                    throw new IllegalStateException(
                            "ERROR: rollback incorrecto"
                    );
                }
                System.out.println("ROLLBACK CORRECTO");
            }

            System.out.println("PRUEBAS FINALIZADAS");
            logger.info("FIN DE BOOKVERSE - PRUEBAS FINALIZADAS");

        } catch (SQLException | RuntimeException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int consultarStock(
            Connection conexion, int idLibro
    ) throws SQLException {

        try (PreparedStatement ps = conexion.prepareStatement("""
                SELECT stock FROM inventario WHERE id_libro = ?
                """)) {

            ps.setInt(1, idLibro);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "Inventario inexistente: " + idLibro
                    );
                }
                return rs.getInt("stock");
            }
        }
    }
}


