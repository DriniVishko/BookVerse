package com.bookverse.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseSeeder {

    public static void insertarDatosIniciales(Connection conexion)
            throws SQLException {

        LibroDao libroDao = new LibroDao();
        AutorDao autorDao = new AutorDao();

        // 1. Insertar libros y recuperar sus IDs
        int idIt = libroDao.insertarLibro(
                conexion,
                "IT",
                "9781501142970",
                new BigDecimal("19.95"),
                "TAPA_BLANDA",
                "Scribner"
        );

        int id1984 = libroDao.insertarLibro(
                conexion,
                "1984",
                "9780451524935",
                new BigDecimal("12.50"),
                "TAPA_BLANDA",
                "Signet"
        );

        // 2. Insertar autores
        int idKing = autorDao.insertarAutor(
                conexion, "Stephen King"
        );

        int idOrwell = autorDao.insertarAutor(
                conexion, "George Orwell"
        );

        // 3. Asociar autores y libros
        autorDao.asociarAutorLibro(conexion, idIt, idKing);
        autorDao.asociarAutorLibro(conexion, id1984, idOrwell);

        insertarClientes(conexion);
        insertarInventario(conexion, idIt, id1984);

        System.out.println("Datos iniciales insertados.");
    }


    private static void insertarClientes(Connection conexion)
            throws SQLException {

        String sql = """
            INSERT INTO cliente
            (nombre, email, telefono, direccion, ciudad, codigo_postal)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, "Ana García");
            ps.setString(2, "ana@bookverse.com");
            ps.setString(3, "600111222");
            ps.setString(4, "Calle Mayor 10");
            ps.setString(5, "Madrid");
            ps.setString(6, "28013");
            ps.addBatch();

            ps.setString(1, "Carlos López");
            ps.setString(2, "carlos@bookverse.com");
            ps.setString(3, "600333444");
            ps.setString(4, "Avenida del Puerto 25");
            ps.setString(5, "Valencia");
            ps.setString(6, "46021");
            ps.addBatch();

            ps.executeBatch();
        }
    }

    private static void insertarInventario(
            Connection conexion, int idIt, int id1984)
            throws SQLException {

        String sql = """
            INSERT INTO inventario
            (id_libro, stock, stock_minimo)
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idIt);
            ps.setInt(2, 15);
            ps.setInt(3, 5);
            ps.addBatch();

            ps.setInt(1, id1984);
            ps.setInt(2, 10);
            ps.setInt(3, 5);
            ps.addBatch();

            ps.executeBatch();
        }
    }


}
