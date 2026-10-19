package com.taller.modelo;

import java.math.BigDecimal;

public class Repuesto {

    private String idRepuesto;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int existencia;

    public Repuesto(String idRepuesto, String nombre, String descripcion, BigDecimal precio, int existencia) {
        this.idRepuesto = idRepuesto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.existencia = existencia;
    }

    public String getIdRepuesto() {
        return idRepuesto;
    }

    public void setIdRepuesto(String idRepuesto) {
        this.idRepuesto = idRepuesto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getExistencia() {
        return existencia;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }

    public void abastecer(int cantidad) {
        existencia += cantidad;
    }

    public void descontar(int cantidad) {
        existencia -= cantidad;
    }

    public boolean hayExistencia(int cantidad) {
        return cantidad > 0 && cantidad <= existencia;
    }

    public boolean estaAgotado() {
        return existencia == 0;
    }

    @Override
    public String toString() {
        return nombre + " (" + existencia + " disp.)";
    }
}
