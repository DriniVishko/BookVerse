package com.bookverse.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutorDAO {

    public int insertarAutor(Connection conexion, String nombre) throws SQLException {
        String sql="INSERT INTO autor (nombre) VALUES (?)";
        try(PreparedStatement ps = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){
            ps.setString(1, nombre);
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys()){
                if(rs.next()){
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo insertar el autor en la base de datos");
    }

    public void asociarAutorLibro(Connection conexion, int idLibro, int idAutor) throws SQLException{
        String sql="INSERT INTO libro_autor (id_libro, id_autor) VALUES (?, ?)";
        try(PreparedStatement ps = conexion.prepareStatement(sql)){
            ps.setInt(1, idLibro);
            ps.setInt(2, idAutor);
            ps.executeUpdate();
        }
    }


}
