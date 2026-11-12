package com.bookverse.model;

import java.math.BigDecimal;

public class Libro {

    private int idLibro;
    private String titulo;
    private String isbn;
    private BigDecimal precio;
    private String formato;
    private String editorial;

    public Libro(int idLibro, String titulo, String isbn,
                 BigDecimal precio, String formato, String editorial) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.isbn = isbn;
        this.precio = precio;
        this.formato = formato;
        this.editorial = editorial;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getIsbn() {
        return isbn;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public String getFormato() {
        return formato;
    }

    public String getEditorial() {
        return editorial;
    }

    @Override
    public String toString() {
        return
                "idLibro=" + idLibro +
                ", titulo='" + titulo + '\'' +
                ", isbn='" + isbn + '\'' +
                ", precio=" + precio +
                ", formato='" + formato + '\'' +
                ", editorial='" + editorial + '\'';
    }
}
