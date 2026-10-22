package com.taller.dao;

import com.taller.modelo.EstadoOrden;
import com.taller.modelo.Repuesto;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class ReporteDAO {

    private final OrdenDAO ordenDAO = new OrdenDAO();
    private final RepuestoDAO repuestoDAO = new RepuestoDAO();

    public ArrayList<Object[]> ordenesPorEstado(EstadoOrden estado) throws SQLException {
        return ordenDAO.listar(estado);
    }

    public ArrayList<Object[]> reparacionesPorVehiculo(String placa) throws SQLException {
        String sql = "SELECT o.id_orden, o.fecha_creacion, o.fecha_finalizacion, "
                + "m.nombre || ' ' || m.apellido, o.estado, "
                + "(SELECT LISTAGG(s.descripcion, ', ') WITHIN GROUP (ORDER BY ds.id_detalle) "
                + "   FROM detalle_servicio ds JOIN servicio s ON ds.id_servicio = s.id_servicio "
                + "  WHERE ds.id_orden = o.id_orden), "
                + "(SELECT LISTAGG(r.nombre || ' x' || dr.cantidad, ', ') WITHIN GROUP (ORDER BY dr.id_detalle) "
                + "   FROM detalle_repuesto dr JOIN repuesto r ON dr.id_repuesto = r.id_repuesto "
                + "  WHERE dr.id_orden = o.id_orden), "
                + "o.total "
                + "FROM orden_trabajo o JOIN mecanico m ON o.id_mecanico = m.id_mecanico "
                + "WHERE UPPER(o.placa) = UPPER(?) "
                + "ORDER BY o.fecha_creacion DESC, o.id_orden DESC";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, placa.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{
                        rs.getInt(1), rs.getDate(2), rs.getDate(3), rs.getString(4), rs.getString(5),
                        texto(rs.getString(6)), texto(rs.getString(7)), rs.getBigDecimal(8)
                    });
                }
            }
        }
        return filas;
    }

    public ArrayList<Object[]> repuestosBajoInventario() throws SQLException {
        ArrayList<Object[]> filas = new ArrayList<>();
        for (Repuesto r : repuestoDAO.listarBajoInventario()) {
            filas.add(new Object[]{
                r.getIdRepuesto(), r.getNombre(), r.getDescripcion(), r.getPrecio(), r.getExistencia(),
                r.estaAgotado() ? "AGOTADO" : "BAJO"
            });
        }
        return filas;
    }

    public ArrayList<Object[]> reparacionesPorMecanico(String idMecanico) throws SQLException {
        String sql = "SELECT m.nombre || ' ' || m.apellido, o.id_orden, o.fecha_creacion, o.placa, o.estado, o.total "
                + "FROM orden_trabajo o JOIN mecanico m ON o.id_mecanico = m.id_mecanico "
                + (idMecanico == null ? "" : "WHERE m.id_mecanico = ? ")
                + "ORDER BY m.apellido, m.nombre, o.id_orden DESC";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idMecanico != null) {
                ps.setString(1, idMecanico);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{
                        rs.getString(1), rs.getInt(2), rs.getDate(3), rs.getString(4), rs.getString(5), rs.getBigDecimal(6)
                    });
                }
            }
        }
        return filas;
    }

    public ArrayList<Object[]> resumenPorMecanico() throws SQLException {
        String sql = "SELECT m.nombre || ' ' || m.apellido, "
                + "COUNT(o.id_orden), "
                + "SUM(CASE WHEN o.estado = 'PENDIENTE' THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN o.estado = 'EN PROCESO' THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN o.estado = 'FINALIZADA' THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN o.estado = 'CANCELADA' THEN 1 ELSE 0 END) "
                + "FROM mecanico m LEFT JOIN orden_trabajo o ON o.id_mecanico = m.id_mecanico "
                + "GROUP BY m.id_mecanico, m.nombre, m.apellido "
                + "ORDER BY COUNT(o.id_orden) DESC, m.apellido";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new Object[]{
                    rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6)
                });
            }
        }
        return filas;
    }

    public ArrayList<Object[]> ingresos(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT o.id_orden, o.fecha_finalizacion, o.placa, c.nombre || ' ' || c.apellido, "
                + "m.nombre || ' ' || m.apellido, o.total_mano_obra, o.total_repuestos, o.total "
                + "FROM orden_trabajo o "
                + "JOIN vehiculo v ON o.placa = v.placa "
                + "JOIN cliente c ON v.id_cliente = c.id_cliente "
                + "JOIN mecanico m ON o.id_mecanico = m.id_mecanico "
                + "WHERE o.estado = ? AND o.fecha_finalizacion >= ? AND o.fecha_finalizacion < ? "
                + "ORDER BY o.fecha_finalizacion, o.id_orden";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, EstadoOrden.FINALIZADA.getValorBD());
            ps.setDate(2, Date.valueOf(desde));
            ps.setDate(3, Date.valueOf(hasta.plusDays(1)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{
                        rs.getInt(1), rs.getDate(2), rs.getString(3), rs.getString(4), rs.getString(5),
                        rs.getBigDecimal(6), rs.getBigDecimal(7), rs.getBigDecimal(8)
                    });
                }
            }
        }
        return filas;
    }

    private String texto(String valor) {
        return valor == null ? "-" : valor;
    }
}
