package com.taller.dao;

import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Mecanico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MecanicoDAO {

    public void insertar(Mecanico m) throws SQLException, ValidacionException {
        if (buscarPorId(m.getId()) != null) {
            throw new ValidacionException("Ya existe un mecánico con el identificador " + m.getId());
        }
        String sql = "INSERT INTO mecanico (id_mecanico, nombre, apellido, telefono, especialidad) VALUES (?,?,?,?,?)";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, m.getId());
            ps.setString(2, m.getNombre());
            ps.setString(3, m.getApellido());
            ps.setString(4, m.getTelefono());
            ps.setString(5, m.getEspecialidad());
            ps.executeUpdate();
        }
    }

    public void actualizar(Mecanico m) throws SQLException {
        String sql = "UPDATE mecanico SET nombre = ?, apellido = ?, telefono = ?, especialidad = ? WHERE id_mecanico = ?";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, m.getNombre());
            ps.setString(2, m.getApellido());
            ps.setString(3, m.getTelefono());
            ps.setString(4, m.getEspecialidad());
            ps.setString(5, m.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(String id) throws SQLException, ValidacionException {
        if (tieneOrdenes(id)) {
            throw new ValidacionException("No se puede eliminar el mecánico porque tiene órdenes de trabajo asociadas.");
        }
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("DELETE FROM mecanico WHERE id_mecanico = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public boolean tieneOrdenes(String id) throws SQLException {
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM orden_trabajo WHERE id_mecanico = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public Mecanico buscarPorId(String id) throws SQLException {
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("SELECT * FROM mecanico WHERE id_mecanico = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public ArrayList<Mecanico> listar() throws SQLException {
        ArrayList<Mecanico> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM mecanico ORDER BY apellido, nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Mecanico mapear(ResultSet rs) throws SQLException {
        return new Mecanico(
                rs.getString("id_mecanico"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("telefono"),
                rs.getString("especialidad"));
    }
}
