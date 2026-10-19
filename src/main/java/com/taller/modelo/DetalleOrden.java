package com.taller.modelo;

import java.math.BigDecimal;

public abstract class DetalleOrden {

    private int idDetalle;

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public abstract BigDecimal calcularSubtotal();

    public abstract String getDescripcion();
}
