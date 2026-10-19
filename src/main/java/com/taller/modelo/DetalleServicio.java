package com.taller.modelo;

import java.math.BigDecimal;

public class DetalleServicio extends DetalleOrden {

    private Servicio servicio;
    private BigDecimal costo;

    public DetalleServicio(Servicio servicio) {
        this.servicio = servicio;
        this.costo = servicio.getCostoManoObra();
    }

    public DetalleServicio(Servicio servicio, BigDecimal costo) {
        this.servicio = servicio;
        this.costo = costo;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    @Override
    public BigDecimal calcularSubtotal() {
        return costo;
    }

    @Override
    public String getDescripcion() {
        return servicio.getDescripcion();
    }
}
