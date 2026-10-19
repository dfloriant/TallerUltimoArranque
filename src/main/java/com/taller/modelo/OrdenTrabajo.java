package com.taller.modelo;

import com.taller.excepciones.ValidacionException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public class OrdenTrabajo {

    private int idOrden;
    private Vehiculo vehiculo;
    private Mecanico mecanico;
    private String descripcionProblema;
    private EstadoOrden estado;
    private LocalDate fechaCreacion;
    private LocalDate fechaFinalizacion;
    private final ArrayList<DetalleServicio> servicios = new ArrayList<>();
    private final ArrayList<DetalleRepuesto> repuestos = new ArrayList<>();

    public OrdenTrabajo(Vehiculo vehiculo, Mecanico mecanico, String descripcionProblema) {
        this.vehiculo = vehiculo;
        this.mecanico = mecanico;
        this.descripcionProblema = descripcionProblema;
        this.estado = EstadoOrden.PENDIENTE;
        this.fechaCreacion = LocalDate.now();
    }

    public int getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Mecanico getMecanico() {
        return mecanico;
    }

    public String getDescripcionProblema() {
        return descripcionProblema;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDate getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDate fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public ArrayList<DetalleServicio> getServicios() {
        return servicios;
    }

    public ArrayList<DetalleRepuesto> getRepuestos() {
        return repuestos;
    }

    public boolean esModificable() {
        return estado == EstadoOrden.PENDIENTE || estado == EstadoOrden.EN_PROCESO;
    }

    public void agregarServicio(Servicio servicio) throws ValidacionException {
        validarEnProceso();
        servicios.add(new DetalleServicio(servicio));
    }

    public void agregarRepuesto(Repuesto repuesto, int cantidad) throws ValidacionException {
        validarEnProceso();
        if (!repuesto.hayExistencia(cantidad)) {
            throw new ValidacionException("Existencia insuficiente de " + repuesto.getNombre());
        }
        repuestos.add(new DetalleRepuesto(repuesto, cantidad));
    }

    public void quitarRepuesto(DetalleRepuesto detalle) throws ValidacionException {
        validarEnProceso();
        repuestos.remove(detalle);
    }

    public BigDecimal getTotalManoObra() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleServicio d : servicios) {
            total = total.add(d.calcularSubtotal());
        }
        return total;
    }

    public BigDecimal getTotalRepuestos() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleRepuesto d : repuestos) {
            total = total.add(d.calcularSubtotal());
        }
        return total;
    }

    public BigDecimal getTotal() {
        return getTotalManoObra().add(getTotalRepuestos());
    }

    public void iniciar() throws ValidacionException {
        if (estado != EstadoOrden.PENDIENTE) {
            throw new ValidacionException("Solo se puede iniciar una orden PENDIENTE.");
        }
        estado = EstadoOrden.EN_PROCESO;
    }

    public void finalizar() throws ValidacionException {
        validarEnProceso();
        if (servicios.isEmpty()) {
            throw new ValidacionException("No se puede finalizar una orden sin servicios registrados.");
        }
        estado = EstadoOrden.FINALIZADA;
        fechaFinalizacion = LocalDate.now();
    }

    public void cancelar() throws ValidacionException {
        if (!esModificable()) {
            throw new ValidacionException("La orden ya está " + estado + ".");
        }
        estado = EstadoOrden.CANCELADA;
    }

    private void validarEnProceso() throws ValidacionException {
        if (estado != EstadoOrden.EN_PROCESO) {
            throw new ValidacionException("La orden debe estar EN PROCESO. Estado actual: " + estado);
        }
    }
}
