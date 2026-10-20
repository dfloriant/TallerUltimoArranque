package com.taller.dao;

import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Cliente;
import com.taller.modelo.Vehiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class VehiculoDAO {

    private static final String SELECT_BASE =
            "SELECT v.placa, v.marca, v.modelo, v.anio, v.color, "
            + "c.id_cliente, c.nombre, c.apellido, c.telefono, c.direccion, c.correo "
            + "FROM vehiculo v JOIN cliente c ON v.id_cliente = c.id_cliente ";

    public void insertar(Vehiculo v) throws SQLException, ValidacionException {
        if (buscarPorPlaca(v.getPlaca()) != null) {
            throw new ValidacionException("Ya existe un vehículo con la placa " + v.getPlaca());
        }
        String sql = "INSERT INTO vehiculo (placa, marca, modelo, anio, color, id_cliente) VALUES (?,?,?,?,?,?)";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, v.getPlaca());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            ps.setInt(4, v.getAnio());
            ps.setString(5, v.getColor());
            ps.setString(6, v.getCliente().getId());
            ps.executeUpdate();
        }
    }

    public void actualizar(Vehiculo v) throws SQLException {
        String sql = "UPDATE vehiculo SET marca = ?, modelo = ?, anio = ?, color = ?, id_cliente = ? WHERE placa = ?";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, v.getMarca());
            ps.setString(2, v.getModelo());
            ps.setInt(3, v.getAnio());
            ps.setString(4, v.getColor());
            ps.setString(5, v.getCliente().getId());
            ps.setString(6, v.getPlaca());
            ps.executeUpdate();
        }
    }

    public void eliminar(String placa) throws SQLException, ValidacionException {
        if (tieneOrdenes(placa)) {
            throw new ValidacionException("No se puede eliminar el vehículo porque tiene órdenes de trabajo registradas.");
        }
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("DELETE FROM vehiculo WHERE placa = ?")) {
            ps.setString(1, placa);
            ps.executeUpdate();
        }
    }

    public boolean tieneOrdenes(String placa) throws SQLException {
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM orden_trabajo WHERE placa = ?")) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public Vehiculo buscarPorPlaca(String placa) throws SQLException {
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE v.placa = ?")) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public ArrayList<Vehiculo> listar() throws SQLException {
        ArrayList<Vehiculo> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY v.placa");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public ArrayList<Vehiculo> listarPorCliente(String idCliente) throws SQLException {
        ArrayList<Vehiculo> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE v.id_cliente = ? ORDER BY v.placa")) {
            ps.setString(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public ArrayList<Object[]> historialOrdenes(String placa) throws SQLException {
        String sql = "SELECT o.id_orden, o.fecha_creacion, m.nombre || ' ' || m.apellido, o.descripcion_problema, o.estado, o.total "
                + "FROM orden_trabajo o JOIN mecanico m ON o.id_mecanico = m.id_mecanico "
                + "WHERE o.placa = ? ORDER BY o.fecha_creacion DESC";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{
                        rs.getInt(1),
                        rs.getDate(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getBigDecimal(6)
                    });
                }
            }
        }
        return filas;
    }

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(
                rs.getString("id_cliente"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getString("correo"));
        Vehiculo vehiculo = new Vehiculo(
                rs.getString("placa"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("anio"),
                rs.getString("color"),
                cliente);
        cliente.agregarVehiculo(vehiculo);
        return vehiculo;
    }
}
