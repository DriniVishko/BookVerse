package com.bookverse.database;

import com.bookverse.model.Libro;

import java.sql.Statement;
import java.sql.ResultSet;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibroDao {

    public int insertarLibro(
            Connection conexion,
            String titulo,
            String isbn,
            BigDecimal precio,
            String formato,
            String editorial
    ) throws SQLException {
        String sql = """
                INSERT INTO libro (titulo, isbn, precio, formato, editorial)
                VALUES (?,?,?,?,?)
                """;
        try(PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, titulo);
            ps.setString(2, isbn);
            ps.setBigDecimal(3, precio);
            ps.setString(4, formato);
            ps.setString(5, editorial);
            ps.executeUpdate();

            try(ResultSet claves = ps.getGeneratedKeys()) {
                if(claves.next()){
                    return claves.getInt(1);
                }
            }

        }
        throw new SQLException("No se pudo insertar el libro en la base de datos");
    }

    public List<Libro> listarLibros(Connection conexion) throws SQLException {
        List<Libro> libros = new ArrayList<>();

        String sql= """
                    SELECT id_libro, titulo, isbn, precio, formato, editorial
                    FROM libro
                    ORDER BY id_libro
                    """;

        try(PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                Libro libro = new Libro(
                        rs.getInt("id_libro"),
                        rs.getString("titulo"),
                        rs.getString("isbn"),
                        rs.getBigDecimal("precio"),
                        rs.getString("formato"),
                        rs.getString("editorial")
                );
                libros.add(libro);
            }
        }
        return libros;
    }

}
