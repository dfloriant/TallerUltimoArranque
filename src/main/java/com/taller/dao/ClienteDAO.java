package com.taller.dao;

import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClienteDAO {

    public void insertar(Cliente c) throws SQLException, ValidacionException {
        if (buscarPorId(c.getId()) != null) {
            throw new ValidacionException("Ya existe un cliente con el identificador " + c.getId());
        }
        String sql = "INSERT INTO cliente (id_cliente, nombre, apellido, telefono, direccion, correo) VALUES (?,?,?,?,?,?)";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getId());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getApellido());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getDireccion());
            ps.setString(6, c.getCorreo());
            ps.executeUpdate();
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE cliente SET nombre = ?, apellido = ?, telefono = ?, direccion = ?, correo = ? WHERE id_cliente = ?";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getDireccion());
            ps.setString(5, c.getCorreo());
            ps.setString(6, c.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(String id) throws SQLException, ValidacionException {
        if (tieneVehiculos(id)) {
            throw new ValidacionException("No se puede eliminar el cliente porque tiene vehículos asociados.");
        }
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("DELETE FROM cliente WHERE id_cliente = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public boolean tieneVehiculos(String id) throws SQLException {
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM vehiculo WHERE id_cliente = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public Cliente buscarPorId(String id) throws SQLException {
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("SELECT * FROM cliente WHERE id_cliente = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public ArrayList<Cliente> listar() throws SQLException {
        ArrayList<Cliente> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM cliente ORDER BY apellido, nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getString("id_cliente"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getString("correo"));
    }
}
