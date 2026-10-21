package com.taller.dao;

import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Servicio;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ServicioDAO {

    public void insertar(Servicio s) throws SQLException, ValidacionException {
        validar(s);
        if (buscarPorId(s.getIdServicio()) != null) {
            throw new ValidacionException("Ya existe un servicio con el identificador " + s.getIdServicio());
        }
        String sql = "INSERT INTO servicio (id_servicio, descripcion, costo_mano_obra) VALUES (?,?,?)";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, s.getIdServicio());
            ps.setString(2, s.getDescripcion());
            ps.setBigDecimal(3, s.getCostoManoObra());
            ps.executeUpdate();
        }
    }

    public void actualizar(Servicio s) throws SQLException, ValidacionException {
        validar(s);
        String sql = "UPDATE servicio SET descripcion = ?, costo_mano_obra = ? WHERE id_servicio = ?";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, s.getDescripcion());
            ps.setBigDecimal(2, s.getCostoManoObra());
            ps.setString(3, s.getIdServicio());
            ps.executeUpdate();
        }
    }

    public void eliminar(String id) throws SQLException, ValidacionException {
        if (estaEnOrdenes(id)) {
            throw new ValidacionException("No se puede eliminar el servicio porque ya fue registrado en órdenes de trabajo.");
        }
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("DELETE FROM servicio WHERE id_servicio = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public boolean estaEnOrdenes(String id) throws SQLException {
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM detalle_servicio WHERE id_servicio = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public Servicio buscarPorId(String id) throws SQLException {
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("SELECT * FROM servicio WHERE id_servicio = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public ArrayList<Servicio> listar() throws SQLException {
        ArrayList<Servicio> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM servicio ORDER BY id_servicio");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private void validar(Servicio s) throws ValidacionException {
        if (s.getCostoManoObra() == null || s.getCostoManoObra().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException("El costo de mano de obra no puede ser negativo.");
        }
    }

    private Servicio mapear(ResultSet rs) throws SQLException {
        return new Servicio(
                rs.getString("id_servicio"),
                rs.getString("descripcion"),
                rs.getBigDecimal("costo_mano_obra"));
    }
}
