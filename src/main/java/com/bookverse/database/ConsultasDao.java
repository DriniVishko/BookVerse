
package com.bookverse.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConsultasDao {

    // DTO para mostrar libros con sus autores.
    public record LibroConAutor(
            int idLibro,
            String titulo,
            String autores,
            BigDecimal precio
    ) {}

    // DTO para estadísticas de ventas.
    public record EstadisticaVenta(
            int idLibro,
            String titulo,
            long unidadesVendidas,
            BigDecimal ingresos
    ) {}

    // 1. PAGINACIÓN SQL: LIMIT + OFFSET
    public List<LibroConAutor> listarCatalogoPaginado(
            Connection conexion, int pagina, int tamano
    ) throws SQLException {

        if (pagina < 1 || tamano < 1) {
            throw new IllegalArgumentException(
                    "La página y el tamaño deben ser positivos"
            );
        }

        String sql = """
                SELECT l.id_libro, l.titulo, l.precio,
                       COALESCE(
                           GROUP_CONCAT(a.nombre ORDER BY a.nombre
                                        SEPARATOR ', '),
                           'Sin autor'
                       ) AS autores
                FROM libro l
                LEFT JOIN libro_autor la
                       ON l.id_libro = la.id_libro
                LEFT JOIN autor a
                       ON la.id_autor = a.id_autor
                GROUP BY l.id_libro, l.titulo, l.precio
                ORDER BY l.id_libro
                LIMIT ? OFFSET ?
                """;

        List<LibroConAutor> libros = new ArrayList<>();

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, tamano);
            ps.setInt(2, (pagina - 1) * tamano);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    libros.add(new LibroConAutor(
                            rs.getInt("id_libro"),
                            rs.getString("titulo"),
                            rs.getString("autores"),
                            rs.getBigDecimal("precio")
                    ));
                }
            }
        }

        return libros;
    }

    // 2. JOIN: libros con autores e inventario.
    public void mostrarCatalogoConStock(Connection conexion)
            throws SQLException {

        String sql = """
                SELECT l.titulo, l.isbn, l.precio,
                       i.stock, a.nombre AS autor
                FROM libro l
                INNER JOIN inventario i
                        ON l.id_libro = i.id_libro
                LEFT JOIN libro_autor la
                       ON l.id_libro = la.id_libro
                LEFT JOIN autor a
                       ON la.id_autor = a.id_autor
                ORDER BY l.titulo, a.nombre
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("=== CATÁLOGO CON AUTORES Y STOCK ===");

            while (rs.next()) {
                System.out.printf(
                        "%s | Autor: %s | ISBN: %s | Precio: %s € | Stock: %d%n",
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("isbn"),
                        rs.getBigDecimal("precio"),
                        rs.getInt("stock")
                );
            }
        }
    }

    // 3. GROUP BY: estadísticas de ventas por libro.
    // Solo contabiliza pedidos PAGADOS.
    public List<EstadisticaVenta> obtenerEstadisticasVentas(
            Connection conexion
    ) throws SQLException {

        String sql = """
                SELECT l.id_libro, l.titulo,
                       COALESCE(SUM(d.cantidad), 0) AS unidades,
                       COALESCE(
                           SUM(d.cantidad * d.precio_unitario),
                           0
                       ) AS ingresos
                FROM libro l
                LEFT JOIN detalle_pedido d
                       ON l.id_libro = d.id_libro
                LEFT JOIN pedido p
                       ON d.id_pedido = p.id_pedido
                WHERE p.estado = 'PAGADO'
                   OR p.id_pedido IS NULL
                GROUP BY l.id_libro, l.titulo
                ORDER BY unidades DESC, l.titulo
                """;

        List<EstadisticaVenta> estadisticas = new ArrayList<>();

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                estadisticas.add(new EstadisticaVenta(
                        rs.getInt("id_libro"),
                        rs.getString("titulo"),
                        rs.getLong("unidades"),
                        rs.getBigDecimal("ingresos")
                ));
            }
        }

        return estadisticas;
    }

    // 4. INVENTARIO: libros que necesitan reposición.
    public void mostrarLibrosBajoMinimo(Connection conexion)
            throws SQLException {

        String sql = """
                SELECT l.id_libro, l.titulo,
                       i.stock, i.stock_minimo
                FROM inventario i
                INNER JOIN libro l
                        ON i.id_libro = l.id_libro
                WHERE i.stock <= i.stock_minimo
                ORDER BY i.stock ASC
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("=== LIBROS QUE NECESITAN REPOSICIÓN ===");

            boolean encontrados = false;

            while (rs.next()) {
                encontrados = true;

                System.out.printf(
                        "ID: %d | %s | Stock: %d | Mínimo: %d%n",
                        rs.getInt("id_libro"),
                        rs.getString("titulo"),
                        rs.getInt("stock"),
                        rs.getInt("stock_minimo")
                );
            }

            if (!encontrados) {
                System.out.println("No hay libros bajo el mínimo.");
            }
        }
    }
}

