package com.taller.dao;

import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Repuesto;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class RepuestoDAO {

    public static final int LIMITE_BAJO_INVENTARIO = 5;

    public void insertar(Repuesto r) throws SQLException, ValidacionException {
        validar(r);
        if (r.getExistencia() < 0) {
            throw new ValidacionException("La existencia no puede ser negativa.");
        }
        if (buscarPorId(r.getIdRepuesto()) != null) {
            throw new ValidacionException("Ya existe un repuesto con el identificador " + r.getIdRepuesto());
        }
        String sql = "INSERT INTO repuesto (id_repuesto, nombre, descripcion, precio, existencia) VALUES (?,?,?,?,?)";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, r.getIdRepuesto());
            ps.setString(2, r.getNombre());
            ps.setString(3, r.getDescripcion());
            ps.setBigDecimal(4, r.getPrecio());
            ps.setInt(5, r.getExistencia());
            ps.executeUpdate();
        }
    }

    public void actualizar(Repuesto r) throws SQLException, ValidacionException {
        validar(r);
        String sql = "UPDATE repuesto SET nombre = ?, descripcion = ?, precio = ? WHERE id_repuesto = ?";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, r.getNombre());
            ps.setString(2, r.getDescripcion());
            ps.setBigDecimal(3, r.getPrecio());
            ps.setString(4, r.getIdRepuesto());
            ps.executeUpdate();
        }
    }

    public void eliminar(String id) throws SQLException, ValidacionException {
        if (tieneMovimientos(id)) {
            throw new ValidacionException("No se puede eliminar el repuesto porque ya fue usado en órdenes o tiene abastecimientos registrados.");
        }
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("DELETE FROM repuesto WHERE id_repuesto = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public boolean tieneMovimientos(String id) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM detalle_repuesto WHERE id_repuesto = ?) "
                + "+ (SELECT COUNT(*) FROM abastecimiento WHERE id_repuesto = ?) FROM dual";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void abastecer(String id, int cantidad) throws SQLException, ValidacionException {
        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad a abastecer debe ser mayor que cero.");
        }
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psUpd = cn.prepareStatement("UPDATE repuesto SET existencia = existencia + ? WHERE id_repuesto = ?");
                 PreparedStatement psIns = cn.prepareStatement("INSERT INTO abastecimiento (id_repuesto, cantidad, fecha) VALUES (?, ?, SYSDATE)")) {
                psUpd.setInt(1, cantidad);
                psUpd.setString(2, id);
                if (psUpd.executeUpdate() == 0) {
                    throw new ValidacionException("No existe un repuesto con el identificador " + id);
                }
                psIns.setString(1, id);
                psIns.setInt(2, cantidad);
                psIns.executeUpdate();
                cn.commit();
            } catch (SQLException | ValidacionException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public ArrayList<Object[]> historialAbastecimiento(String id) throws SQLException {
        ArrayList<Object[]> filas = new ArrayList<>();
        String sql = "SELECT fecha, cantidad FROM abastecimiento WHERE id_repuesto = ? ORDER BY fecha DESC";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{rs.getDate(1), rs.getInt(2)});
                }
            }
        }
        return filas;
    }

    public Repuesto buscarPorId(String id) throws SQLException {
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement("SELECT * FROM repuesto WHERE id_repuesto = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public ArrayList<Repuesto> listar() throws SQLException {
        return consultar("SELECT * FROM repuesto ORDER BY id_repuesto");
    }

    public ArrayList<Repuesto> listarAgotados() throws SQLException {
        return consultar("SELECT * FROM repuesto WHERE existencia = 0 ORDER BY id_repuesto");
    }

    public ArrayList<Repuesto> listarBajoInventario() throws SQLException {
        return consultar("SELECT * FROM repuesto WHERE existencia <= " + LIMITE_BAJO_INVENTARIO + " ORDER BY existencia, id_repuesto");
    }

    private ArrayList<Repuesto> consultar(String sql) throws SQLException {
        ArrayList<Repuesto> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private void validar(Repuesto r) throws ValidacionException {
        if (r.getPrecio() == null || r.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException("El precio no puede ser negativo.");
        }
    }

    private Repuesto mapear(ResultSet rs) throws SQLException {
        return new Repuesto(
                rs.getString("id_repuesto"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBigDecimal("precio"),
                rs.getInt("existencia"));
    }
}
