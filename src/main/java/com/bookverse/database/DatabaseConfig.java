
package com.bookverse.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {

    private final Properties properties = new Properties();

    public DatabaseConfig() {
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "No se encuentra application.properties"
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error al cargar application.properties", e
            );
        }
    }

    public String get(String clave) {
        String valor = properties.getProperty(clave);

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "Falta la propiedad: " + clave
            );
        }

        return valor.trim();
    }

    public int getInt(String clave) {
        return Integer.parseInt(get(clave));
    }
}

