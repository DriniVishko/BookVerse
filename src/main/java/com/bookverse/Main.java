
package com.bookverse;

import com.bookverse.database.ConnectionFactory;
import com.bookverse.database.ConsultasDao;
import com.bookverse.database.DatabaseConfig;
import com.bookverse.database.DatabaseInitializer;
import com.bookverse.database.DatabaseSeeder;
import com.bookverse.database.ProcedimientosDao;
import com.bookverse.model.LineaPedido;
import com.bookverse.service.InventarioService;
import com.bookverse.service.PedidoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("INICIO DE BOOKVERSE");

        DatabaseConfig config = new DatabaseConfig();

        try (ConnectionFactory factory = new ConnectionFactory(config);
             Connection conexion = factory.getConnection();
             Scanner scanner = new Scanner(System.in)) {

            System.out.println(
                    "Motor: " + factory.getEngine()
                            + " | Conexión: " + factory.getMode()
            );

            inicializarBaseDeDatos(conexion);

            ConsultasDao consultas = new ConsultasDao();
            ProcedimientosDao procedimientos = new ProcedimientosDao();
            InventarioService inventario = new InventarioService();
            PedidoService pedidos = new PedidoService();

            boolean salir = false;

            while (!salir) {
                mostrarMenu();

                int opcion = leerEntero(scanner, "Selecciona una opción: ");

                try {
                    switch (opcion) {
                        case 1 -> consultas.mostrarCatalogoConStock(conexion);

                        case 2 -> {
                            int pagina = leerEntero(scanner, "Número de página: ");
                            int tamano = leerEntero(scanner, "Libros por página: ");

                            consultas.listarCatalogoPaginado(
                                    conexion, pagina, tamano
                            ).forEach(System.out::println);
                        }

                        case 3 -> {
                            System.out.println("=== ESTADÍSTICAS DE VENTAS ===");

                            consultas.obtenerEstadisticasVentas(conexion)
                                    .forEach(System.out::println);
                        }

                        case 4 -> consultas.mostrarLibrosBajoMinimo(conexion);

                        case 5 -> {
                            int idLibro = leerEntero(scanner, "ID del libro: ");
                            int cantidad = leerEntero(
                                    scanner, "Unidades que quieres añadir: "
                            );

                            inventario.reponerStock(
                                    conexion, idLibro, cantidad
                            );

                            System.out.println(
                                    "Stock actualizado: "
                                            + consultarStock(conexion, idLibro)
                            );
                        }

                        case 6 -> {
                            int idCliente = leerEntero(
                                    scanner, "ID del cliente: "
                            );

                            int numeroLineas = leerEntero(
                                    scanner, "Número de libros diferentes: "
                            );

                            if (numeroLineas <= 0) {
                                System.out.println(
                                        "Debes introducir al menos un libro."
                                );
                                break;
                            }

                            List<LineaPedido> lineas = new ArrayList<>();

                            for (int i = 0; i < numeroLineas; i++) {
                                System.out.println(
                                        "Libro " + (i + 1)
                                );

                                int idLibro = leerEntero(
                                        scanner, "ID del libro: "
                                );

                                int cantidad = leerEntero(
                                        scanner, "Cantidad: "
                                );

                                lineas.add(
                                        new LineaPedido(idLibro, cantidad)
                                );
                            }

                            int idPedido = pedidos.realizarPedido(
                                    conexion, idCliente, lineas, false
                            );

                            System.out.println(
                                    "Pedido realizado correctamente. ID: "
                                            + idPedido
                            );

                            logger.info(
                                    "Pedido creado correctamente: id={}",
                                    idPedido
                            );
                        }

                        case 7 -> {
                            int idPedido = leerEntero(
                                    scanner, "ID del pedido a cancelar: "
                            );

                            pedidos.cancelarPedido(conexion, idPedido);

                            System.out.println(
                                    "Operación de cancelación finalizada."
                            );

                            logger.info(
                                    "Cancelación solicitada: pedido={}",
                                    idPedido
                            );
                        }

                        case 8 -> {
                            if (!factory.getEngine().equalsIgnoreCase("MYSQL")) {
                                System.out.println(
                                        "El procedimiento almacenado "
                                                + "solo está disponible en MySQL."
                                );
                                break;
                            }

                            int idLibro = leerEntero(
                                    scanner, "ID del libro: "
                            );

                            int stock = procedimientos
                                    .consultarStockConProcedimiento(
                                            conexion, idLibro
                                    );

                            System.out.println(
                                    "Stock obtenido con CallableStatement: "
                                            + stock
                            );
                        }

                        case 9 -> demostrarRollback(conexion, pedidos);

                        case 10 -> procedimientos
                                .provocarErrorIntegridad(conexion);

                        case 0 -> {
                            salir = true;
                            System.out.println("Hasta pronto.");
                        }

                        default -> System.out.println(
                                "Opción no válida."
                        );
                    }

                } catch (SQLException | IllegalArgumentException
                         | IllegalStateException e) {

                    System.out.println(
                            "No se pudo completar la operación: "
                                    + e.getMessage()
                    );

                    logger.error(
                            "Error durante la opción {}",
                            opcion,
                            e
                    );
                }
            }

            logger.info("FIN DE BOOKVERSE");

        } catch (SQLException | RuntimeException e) {
            logger.error("Error general de BookVerse", e);
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    private static void inicializarBaseDeDatos(Connection conexion)
            throws SQLException {

        // DEMOSTRACIÓN: reinicia los datos en cada ejecución.
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

        logger.info("Base de datos inicializada correctamente");
    }

    private static void mostrarMenu() {
        System.out.println("""

                ========== BOOKVERSE ==========
                1. Ver catálogo de libros
                2. Ver catálogo paginado
                3. Consultar estadísticas de ventas
                4. Consultar libros bajo mínimo
                5. Reponer stock
                6. Realizar pedido
                7. Cancelar pedido
                8. Consultar stock con procedimiento MySQL
                9. Demostrar rollback
                10. Provocar error SQL controlado
                0. Salir
                ===============================
                """);
    }

    private static int leerEntero(
            Scanner scanner, String mensaje
    ) {
        while (true) {
            System.out.print(mensaje);

            String entrada = scanner.nextLine().trim();

            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Introduce un número entero válido."
                );
            }
        }
    }

    private static void demostrarRollback(
            Connection conexion,
            PedidoService pedidos
    ) throws SQLException {

        int stockAntes = consultarStock(conexion, 1);
        int pedidosAntes = contarPedidos(conexion);

        System.out.println("Stock antes del fallo: " + stockAntes);

        boolean falloDetectado = false;

        try {
            pedidos.realizarPedido(
                    conexion, 1, 1, 2, true
            );
        } catch (SQLException e) {
            falloDetectado = true;

            System.out.println(
                    "Fallo simulado esperado: " + e.getMessage()
            );

            logger.warn(
                    "Fallo de transacción simulado: {}",
                    e.getMessage()
            );
        }

        int stockDespues = consultarStock(conexion, 1);
        int pedidosDespues = contarPedidos(conexion);

        System.out.println(
                "Stock después del rollback: " + stockDespues
        );

        if (falloDetectado
                && stockAntes == stockDespues
                && pedidosAntes == pedidosDespues) {

            System.out.println("ROLLBACK CORRECTO");

            logger.info(
                    "ROLLBACK CORRECTO: stock={}, pedidos={}",
                    stockDespues,
                    pedidosDespues
            );

        } else {
            throw new IllegalStateException(
                    "El rollback no se ha verificado correctamente"
            );
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

    private static int contarPedidos(
            Connection conexion
    ) throws SQLException {

        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT COUNT(*) FROM pedido"
        );
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        }
    }
}
