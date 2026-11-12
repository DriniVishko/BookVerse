
package com.bookverse;

import com.bookverse.database.AutorDAO;
import com.bookverse.database.DatabaseInitializer;
import com.bookverse.database.LibroDao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.bookverse.model.Libro;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/unidad2";
        String usuario = "root";
        String password = System.getenv("BOOKVERSE_DB_PASSWORD");

        try (Connection conexion =
                     DriverManager.getConnection(
                             url, usuario, password)) {

            System.out.println("Conexión correcta con MySQL");

            System.out.println("Base de datos: " + conexion.getCatalog());

            DatabaseInitializer.crearTablaLibros(conexion);
            DatabaseInitializer.crearTablaAutor(conexion);
            DatabaseInitializer.crearTablaLibroAutor(conexion);

            LibroDao libroDao = new LibroDao();

            List<Libro> libros = libroDao.listarLibros(conexion);

            System.out.println("Libros en la base de datos:");
            for(Libro libro: libros){
                System.out.println(libro);
            }

            AutorDAO autorDAO = new AutorDAO();

            int idAutor = autorDAO.insertarAutor(conexion, "George Orwell");

            autorDAO.asociarAutorLibro(conexion, 2, idAutor);

            System.out.println("Autor insertado y asociado correctamente.");


        } catch (SQLException e) {

            System.out.println(
                    "Error al conectar: " + e.getMessage()
            );
        }
    }
}
