
package com.bookverse.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ProcedimientosDao {

    private static final Logger logger =
            LogManager.getLogger(ProcedimientosDao.class);

    // Procedimiento almacenado MySQL con IN y OUT.
    public int consultarStockConProcedimiento(
            Connection conexion,
            int idLibro
    ) throws SQLException {

        String sql = "{CALL consultar_stock_libro(?, ?)}";

        try (CallableStatement cs = conexion.prepareCall(sql)) {

            cs.setInt(1, idLibro);
            cs.registerOutParameter(2, Types.INTEGER);

            cs.execute();

            int stock = cs.getInt(2);

            if (cs.wasNull()) {
                throw new SQLException(
                        "El procedimiento no encontró el libro: " + idLibro
                );
            }

            logger.info(
                    "Procedimiento almacenado ejecutado: libro={}, stock={}",
                    idLibro,
                    stock
            );

            return stock;
        }
    }

    // Provoca una violación de la restricción UNIQUE del ISBN.
    public void provocarErrorIntegridad(Connection conexion) {

        String sql = """
                INSERT INTO libro (titulo, isbn, precio, formato, editorial)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, "Libro duplicado de prueba");
            ps.setString(2, "9781501142970");
            ps.setBigDecimal(3, new java.math.BigDecimal("10.00"));
            ps.setString(4, "PAPEL");
            ps.setString(5, "Editorial de prueba");

            ps.executeUpdate();

            throw new IllegalStateException(
                    "ERROR: el ISBN duplicado fue aceptado"
            );

        } catch (SQLException e) {

            logger.error(
                    "Error SQL controlado. SQLState={}, codigo={}, mensaje={}",
                    e.getSQLState(),
                    e.getErrorCode(),
                    e.getMessage()
            );

            System.out.println(
                    "Error SQL esperado | SQLState: "
                            + e.getSQLState()
                            + " | Código: "
                            + e.getErrorCode()
            );
        }
    }
}

