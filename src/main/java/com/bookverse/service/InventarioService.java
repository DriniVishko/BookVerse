
package com.bookverse.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InventarioService {

    private static final Logger logger =
            LogManager.getLogger(InventarioService.class);

    public void reponerStock(
            Connection conexion,
            int idLibro,
            int cantidad
    ) throws SQLException {

        if (idLibro <= 0 || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "El libro y la cantidad deben ser positivos"
            );
        }

        boolean autoCommitOriginal = conexion.getAutoCommit();

        try {
            conexion.setAutoCommit(false);

            String actualizarStock = """
                    UPDATE inventario
                    SET stock = stock + ?
                    WHERE id_libro = ?
                    """;

            int filasActualizadas;

            try (PreparedStatement ps =
                         conexion.prepareStatement(actualizarStock)) {

                ps.setInt(1, cantidad);
                ps.setInt(2, idLibro);

                filasActualizadas = ps.executeUpdate();
            }

            if (filasActualizadas != 1) {
                throw new SQLException(
                        "No existe inventario para el libro " + idLibro
                );
            }

            String registrarMovimiento = """
                    INSERT INTO movimiento_stock
                    (id_libro, tipo, cantidad, fecha)
                    VALUES (?, 'ENTRADA', ?, CURRENT_TIMESTAMP)
                    """;

            try (PreparedStatement ps =
                         conexion.prepareStatement(registrarMovimiento)) {

                ps.setInt(1, idLibro);
                ps.setInt(2, cantidad);
                ps.executeUpdate();
            }

            conexion.commit();

            logger.info(
                    "Reposición completada: libro={}, unidades={}",
                    idLibro,
                    cantidad
            );

        } catch (SQLException | RuntimeException e) {

            try {
                conexion.rollback();
                logger.warn(
                        "ROLLBACK en reposición: libro={}, motivo={}",
                        idLibro,
                        e.getMessage()
                );
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
                logger.error(
                        "Error al ejecutar rollback de reposición",
                        rollbackError
                );
            }

            throw e;

        } finally {
            conexion.setAutoCommit(autoCommitOriginal);
        }
    }
}

