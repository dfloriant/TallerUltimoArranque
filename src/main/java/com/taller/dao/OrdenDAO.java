package com.taller.dao;

import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Cliente;
import com.taller.modelo.DetalleRepuesto;
import com.taller.modelo.DetalleServicio;
import com.taller.modelo.EstadoOrden;
import com.taller.modelo.Mecanico;
import com.taller.modelo.OrdenTrabajo;
import com.taller.modelo.Repuesto;
import com.taller.modelo.Servicio;
import com.taller.modelo.Vehiculo;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrdenDAO {

    private static final String SQL_TOTALES =
            "UPDATE orden_trabajo SET "
            + "total_mano_obra = (SELECT NVL(SUM(costo), 0) FROM detalle_servicio WHERE id_orden = ?), "
            + "total_repuestos = (SELECT NVL(SUM(subtotal), 0) FROM detalle_repuesto WHERE id_orden = ?), "
            + "total = (SELECT NVL(SUM(costo), 0) FROM detalle_servicio WHERE id_orden = ?) "
            + "+ (SELECT NVL(SUM(subtotal), 0) FROM detalle_repuesto WHERE id_orden = ?) "
            + "WHERE id_orden = ?";

    public int crear(OrdenTrabajo orden) throws SQLException, ValidacionException {
        if (orden.getVehiculo() == null || orden.getMecanico() == null) {
            throw new ValidacionException("Debe seleccionar un vehículo y un mecánico.");
        }
        if (orden.getDescripcionProblema() == null || orden.getDescripcionProblema().isBlank()) {
            throw new ValidacionException("Debe ingresar la descripción del problema.");
        }
        String sql = "INSERT INTO orden_trabajo (placa, id_mecanico, descripcion_problema, estado, fecha_creacion) VALUES (?, ?, ?, ?, SYSDATE)";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql, new String[]{"ID_ORDEN"})) {
            ps.setString(1, orden.getVehiculo().getPlaca());
            ps.setString(2, orden.getMecanico().getId());
            ps.setString(3, orden.getDescripcionProblema());
            ps.setString(4, EstadoOrden.PENDIENTE.getValorBD());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                int id = rs.getInt(1);
                orden.setIdOrden(id);
                return id;
            }
        }
    }

    public ArrayList<Object[]> listar(EstadoOrden estado) throws SQLException {
        String sql = "SELECT o.id_orden, o.fecha_creacion, o.placa, c.nombre || ' ' || c.apellido, "
                + "m.nombre || ' ' || m.apellido, o.estado, o.total "
                + "FROM orden_trabajo o "
                + "JOIN vehiculo v ON o.placa = v.placa "
                + "JOIN cliente c ON v.id_cliente = c.id_cliente "
                + "JOIN mecanico m ON o.id_mecanico = m.id_mecanico "
                + (estado == null ? "" : "WHERE o.estado = ? ")
                + "ORDER BY o.id_orden DESC";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            if (estado != null) {
                ps.setString(1, estado.getValorBD());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{
                        rs.getInt(1), rs.getDate(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getBigDecimal(7)
                    });
                }
            }
        }
        return filas;
    }

    public OrdenTrabajo buscarPorId(int idOrden) throws SQLException {
        String sql = "SELECT o.*, v.marca, v.modelo, v.anio, v.color, "
                + "c.id_cliente, c.nombre AS c_nombre, c.apellido AS c_apellido, c.telefono AS c_telefono, c.direccion, c.correo, "
                + "m.nombre AS m_nombre, m.apellido AS m_apellido, m.telefono AS m_telefono, m.especialidad "
                + "FROM orden_trabajo o "
                + "JOIN vehiculo v ON o.placa = v.placa "
                + "JOIN cliente c ON v.id_cliente = c.id_cliente "
                + "JOIN mecanico m ON o.id_mecanico = m.id_mecanico "
                + "WHERE o.id_orden = ?";
        try (Connection cn = Conexion.getConexion()) {
            OrdenTrabajo orden;
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, idOrden);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    orden = mapearOrden(rs);
                }
            }
            cargarServicios(cn, orden);
            cargarRepuestos(cn, orden);
            return orden;
        }
    }

    public void iniciar(int idOrden) throws SQLException, ValidacionException {
        try (Connection cn = Conexion.getConexion()) {
            EstadoOrden estado = estadoActual(cn, idOrden, false);
            if (estado != EstadoOrden.PENDIENTE) {
                throw new ValidacionException("Solo se puede iniciar una orden PENDIENTE. Estado actual: " + estado);
            }
            cambiarEstado(cn, idOrden, EstadoOrden.EN_PROCESO);
        }
    }

    public void agregarServicio(int idOrden, Servicio servicio) throws SQLException, ValidacionException {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                validarEnProceso(cn, idOrden);
                try (PreparedStatement ps = cn.prepareStatement("INSERT INTO detalle_servicio (id_orden, id_servicio, costo) VALUES (?, ?, ?)")) {
                    ps.setInt(1, idOrden);
                    ps.setString(2, servicio.getIdServicio());
                    ps.setBigDecimal(3, servicio.getCostoManoObra());
                    ps.executeUpdate();
                }
                actualizarTotales(cn, idOrden);
                cn.commit();
            } catch (SQLException | ValidacionException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public void quitarServicio(int idOrden, int idDetalle) throws SQLException, ValidacionException {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                validarEnProceso(cn, idOrden);
                try (PreparedStatement ps = cn.prepareStatement("DELETE FROM detalle_servicio WHERE id_detalle = ? AND id_orden = ?")) {
                    ps.setInt(1, idDetalle);
                    ps.setInt(2, idOrden);
                    ps.executeUpdate();
                }
                actualizarTotales(cn, idOrden);
                cn.commit();
            } catch (SQLException | ValidacionException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public void agregarRepuesto(int idOrden, String idRepuesto, int cantidad) throws SQLException, ValidacionException {
        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad debe ser mayor que cero.");
        }
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                validarEnProceso(cn, idOrden);
                int existencia;
                BigDecimal precio;
                String nombre;
                try (PreparedStatement ps = cn.prepareStatement("SELECT existencia, precio, nombre FROM repuesto WHERE id_repuesto = ? FOR UPDATE")) {
                    ps.setString(1, idRepuesto);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ValidacionException("El repuesto no existe.");
                        }
                        existencia = rs.getInt(1);
                        precio = rs.getBigDecimal(2);
                        nombre = rs.getString(3);
                    }
                }
                if (cantidad > existencia) {
                    throw new ValidacionException("Existencia insuficiente de " + nombre + ". Disponible: " + existencia + ", solicitado: " + cantidad + ".");
                }
                try (PreparedStatement ps = cn.prepareStatement("INSERT INTO detalle_repuesto (id_orden, id_repuesto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setInt(1, idOrden);
                    ps.setString(2, idRepuesto);
                    ps.setInt(3, cantidad);
                    ps.setBigDecimal(4, precio);
                    ps.setBigDecimal(5, precio.multiply(BigDecimal.valueOf(cantidad)));
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cn.prepareStatement("UPDATE repuesto SET existencia = existencia - ? WHERE id_repuesto = ?")) {
                    ps.setInt(1, cantidad);
                    ps.setString(2, idRepuesto);
                    ps.executeUpdate();
                }
                actualizarTotales(cn, idOrden);
                cn.commit();
            } catch (SQLException | ValidacionException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public void quitarRepuesto(int idOrden, int idDetalle) throws SQLException, ValidacionException {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                validarEnProceso(cn, idOrden);
                String idRepuesto;
                int cantidad;
                try (PreparedStatement ps = cn.prepareStatement("SELECT id_repuesto, cantidad FROM detalle_repuesto WHERE id_detalle = ? AND id_orden = ?")) {
                    ps.setInt(1, idDetalle);
                    ps.setInt(2, idOrden);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ValidacionException("El repuesto seleccionado no pertenece a la orden.");
                        }
                        idRepuesto = rs.getString(1);
                        cantidad = rs.getInt(2);
                    }
                }
                try (PreparedStatement ps = cn.prepareStatement("DELETE FROM detalle_repuesto WHERE id_detalle = ?")) {
                    ps.setInt(1, idDetalle);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cn.prepareStatement("UPDATE repuesto SET existencia = existencia + ? WHERE id_repuesto = ?")) {
                    ps.setInt(1, cantidad);
                    ps.setString(2, idRepuesto);
                    ps.executeUpdate();
                }
                actualizarTotales(cn, idOrden);
                cn.commit();
            } catch (SQLException | ValidacionException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public void finalizar(int idOrden) throws SQLException, ValidacionException {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                validarEnProceso(cn, idOrden);
                try (PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM detalle_servicio WHERE id_orden = ?")) {
                    ps.setInt(1, idOrden);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) == 0) {
                            throw new ValidacionException("No se puede finalizar una orden sin servicios registrados.");
                        }
                    }
                }
                actualizarTotales(cn, idOrden);
                try (PreparedStatement ps = cn.prepareStatement("UPDATE orden_trabajo SET estado = ?, fecha_finalizacion = SYSDATE WHERE id_orden = ?")) {
                    ps.setString(1, EstadoOrden.FINALIZADA.getValorBD());
                    ps.setInt(2, idOrden);
                    ps.executeUpdate();
                }
                cn.commit();
            } catch (SQLException | ValidacionException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public void cancelar(int idOrden) throws SQLException, ValidacionException {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                EstadoOrden estado = estadoActual(cn, idOrden, true);
                if (estado != EstadoOrden.PENDIENTE && estado != EstadoOrden.EN_PROCESO) {
                    throw new ValidacionException("Solo se pueden cancelar órdenes PENDIENTES o EN PROCESO. Estado actual: " + estado);
                }
                String sqlReintegro = "UPDATE repuesto r SET existencia = existencia + "
                        + "(SELECT SUM(d.cantidad) FROM detalle_repuesto d WHERE d.id_orden = ? AND d.id_repuesto = r.id_repuesto) "
                        + "WHERE r.id_repuesto IN (SELECT id_repuesto FROM detalle_repuesto WHERE id_orden = ?)";
                try (PreparedStatement ps = cn.prepareStatement(sqlReintegro)) {
                    ps.setInt(1, idOrden);
                    ps.setInt(2, idOrden);
                    ps.executeUpdate();
                }
                cambiarEstado(cn, idOrden, EstadoOrden.CANCELADA);
                cn.commit();
            } catch (SQLException | ValidacionException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    private EstadoOrden estadoActual(Connection cn, int idOrden, boolean bloquear) throws SQLException, ValidacionException {
        String sql = "SELECT estado FROM orden_trabajo WHERE id_orden = ?" + (bloquear ? " FOR UPDATE" : "");
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ValidacionException("No existe la orden " + idOrden + ".");
                }
                return EstadoOrden.desdeBD(rs.getString(1));
            }
        }
    }

    private void validarEnProceso(Connection cn, int idOrden) throws SQLException, ValidacionException {
        EstadoOrden estado = estadoActual(cn, idOrden, true);
        if (estado != EstadoOrden.EN_PROCESO) {
            throw new ValidacionException("La orden debe estar EN PROCESO para modificarla. Estado actual: " + estado);
        }
    }

    private void cambiarEstado(Connection cn, int idOrden, EstadoOrden estado) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE orden_trabajo SET estado = ? WHERE id_orden = ?")) {
            ps.setString(1, estado.getValorBD());
            ps.setInt(2, idOrden);
            ps.executeUpdate();
        }
    }

    private void actualizarTotales(Connection cn, int idOrden) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_TOTALES)) {
            for (int i = 1; i <= 5; i++) {
                ps.setInt(i, idOrden);
            }
            ps.executeUpdate();
        }
    }

    private OrdenTrabajo mapearOrden(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(rs.getString("id_cliente"), rs.getString("c_nombre"), rs.getString("c_apellido"),
                rs.getString("c_telefono"), rs.getString("direccion"), rs.getString("correo"));
        Vehiculo vehiculo = new Vehiculo(rs.getString("placa"), rs.getString("marca"), rs.getString("modelo"),
                rs.getInt("anio"), rs.getString("color"), cliente);
        Mecanico mecanico = new Mecanico(rs.getString("id_mecanico"), rs.getString("m_nombre"), rs.getString("m_apellido"),
                rs.getString("m_telefono"), rs.getString("especialidad"));
        OrdenTrabajo orden = new OrdenTrabajo(vehiculo, mecanico, rs.getString("descripcion_problema"));
        orden.setIdOrden(rs.getInt("id_orden"));
        orden.setEstado(EstadoOrden.desdeBD(rs.getString("estado")));
        orden.setFechaCreacion(rs.getDate("fecha_creacion").toLocalDate());
        Date finalizacion = rs.getDate("fecha_finalizacion");
        if (finalizacion != null) {
            orden.setFechaFinalizacion(finalizacion.toLocalDate());
        }
        return orden;
    }

    private void cargarServicios(Connection cn, OrdenTrabajo orden) throws SQLException {
        String sql = "SELECT d.id_detalle, d.costo, s.id_servicio, s.descripcion, s.costo_mano_obra "
                + "FROM detalle_servicio d JOIN servicio s ON d.id_servicio = s.id_servicio WHERE d.id_orden = ? ORDER BY d.id_detalle";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, orden.getIdOrden());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Servicio servicio = new Servicio(rs.getString("id_servicio"), rs.getString("descripcion"), rs.getBigDecimal("costo_mano_obra"));
                    DetalleServicio detalle = new DetalleServicio(servicio, rs.getBigDecimal("costo"));
                    detalle.setIdDetalle(rs.getInt("id_detalle"));
                    orden.getServicios().add(detalle);
                }
            }
        }
    }

    private void cargarRepuestos(Connection cn, OrdenTrabajo orden) throws SQLException {
        String sql = "SELECT d.id_detalle, d.cantidad, d.precio_unitario, r.id_repuesto, r.nombre, r.descripcion, r.precio, r.existencia "
                + "FROM detalle_repuesto d JOIN repuesto r ON d.id_repuesto = r.id_repuesto WHERE d.id_orden = ? ORDER BY d.id_detalle";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, orden.getIdOrden());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Repuesto repuesto = new Repuesto(rs.getString("id_repuesto"), rs.getString("nombre"), rs.getString("descripcion"),
                            rs.getBigDecimal("precio"), rs.getInt("existencia"));
                    DetalleRepuesto detalle = new DetalleRepuesto(repuesto, rs.getInt("cantidad"), rs.getBigDecimal("precio_unitario"));
                    detalle.setIdDetalle(rs.getInt("id_detalle"));
                    orden.getRepuestos().add(detalle);
                }
            }
        }
    }
}
