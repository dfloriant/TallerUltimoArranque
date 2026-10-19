package com.taller.modelo;

import java.math.BigDecimal;

public class DetalleRepuesto extends DetalleOrden {

    private Repuesto repuesto;
    private int cantidad;
    private BigDecimal precioUnitario;

    public DetalleRepuesto(Repuesto repuesto, int cantidad) {
        this.repuesto = repuesto;
        this.cantidad = cantidad;
        this.precioUnitario = repuesto.getPrecio();
    }

    public DetalleRepuesto(Repuesto repuesto, int cantidad, BigDecimal precioUnitario) {
        this.repuesto = repuesto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Repuesto getRepuesto() {
        return repuesto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    @Override
    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    @Override
    public String getDescripcion() {
        return repuesto.getNombre() + " x" + cantidad;
    }
}
