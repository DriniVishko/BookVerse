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
            System.out.println("Tabla 'libro' creada correctamente.");
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

    public static void eliminarTablas(Connection conexion) throws SQLException{
        try(Statement st = conexion.createStatement()) {
            st.executeUpdate("DROP TABLE IF EXISTS libro_autor");
            st.executeUpdate("DROP TABLE IF EXISTS autor");
            st.executeUpdate("DROP TABLE IF EXISTS libro");
        }
    }

}
