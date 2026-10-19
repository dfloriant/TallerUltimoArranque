package com.taller.modelo;

import java.math.BigDecimal;

public class Servicio {

    private String idServicio;
    private String descripcion;
    private BigDecimal costoManoObra;

    public Servicio(String idServicio, String descripcion, BigDecimal costoManoObra) {
        this.idServicio = idServicio;
        this.descripcion = descripcion;
        this.costoManoObra = costoManoObra;
    }

    public String getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(String idServicio) {
        this.idServicio = idServicio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getCostoManoObra() {
        return costoManoObra;
    }

    public void setCostoManoObra(BigDecimal costoManoObra) {
        this.costoManoObra = costoManoObra;
    }

    @Override
    public String toString() {
        return descripcion + " (Q" + costoManoObra + ")";
    }
}
