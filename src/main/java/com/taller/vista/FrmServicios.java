package com.taller.vista;

import com.taller.dao.ServicioDAO;
import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Servicio;
import java.sql.SQLException;
import javax.swing.JTextField;

public class FrmServicios extends FormularioBase {

    private final ServicioDAO dao = new ServicioDAO();
    private final JTextField txtId = new JTextField(15);
    private final JTextField txtDescripcion = new JTextField(15);
    private final JTextField txtCosto = new JTextField(15);

    public FrmServicios() {
        super("Gestión de Servicios", new String[]{"Identificador", "Descripción", "Costo mano de obra (Q)"});
        agregarCampo("Identificador:", txtId);
        agregarCampo("Descripción:", txtDescripcion);
        agregarCampo("Costo mano de obra (Q):", txtCosto);
        ejecutar(this::cargarTabla);
    }

    private Servicio leerServicio() throws ValidacionException {
        String id = leer(txtId, "Identificador", true).toUpperCase();
        String descripcion = leer(txtDescripcion, "Descripción", true);
        return new Servicio(id, descripcion, leerDecimal(txtCosto, "Costo mano de obra"));
    }

    @Override
    protected void guardar() throws SQLException, ValidacionException {
        dao.insertar(leerServicio());
        informar("Servicio registrado correctamente.");
        limpiar();
    }

    @Override
    protected void modificar() throws SQLException, ValidacionException {
        Servicio servicio = leerServicio();
        if (dao.buscarPorId(servicio.getIdServicio()) == null) {
            throw new ValidacionException("Seleccione un servicio existente para modificar.");
        }
        dao.actualizar(servicio);
        informar("Servicio modificado correctamente.");
        limpiar();
    }

    @Override
    protected void eliminar() throws SQLException, ValidacionException {
        String id = leer(txtId, "Identificador", true).toUpperCase();
        if (confirmar("¿Desea eliminar el servicio " + id + "?")) {
            dao.eliminar(id);
            informar("Servicio eliminado correctamente.");
            limpiar();
        }
    }

    @Override
    protected void buscar() throws SQLException, ValidacionException {
        String id = leer(txtId, "Identificador", true).toUpperCase();
        Servicio servicio = dao.buscarPorId(id);
        if (servicio == null) {
            throw new ValidacionException("No existe un servicio con el identificador " + id + ".");
        }
        modeloTabla.setRowCount(0);
        agregarFila(servicio);
        tabla.setRowSelectionInterval(0, 0);
    }

    @Override
    protected void limpiar() throws SQLException, ValidacionException {
        txtId.setText("");
        txtDescripcion.setText("");
        txtCosto.setText("");
        txtId.setEditable(true);
        tabla.clearSelection();
        cargarTabla();
    }

    @Override
    protected void cargarTabla() throws SQLException {
        modeloTabla.setRowCount(0);
        for (Servicio s : dao.listar()) {
            agregarFila(s);
        }
    }

    @Override
    protected void alSeleccionarFila(int fila) {
        txtId.setText(valorTabla(fila, 0));
        txtDescripcion.setText(valorTabla(fila, 1));
        txtCosto.setText(valorTabla(fila, 2));
        txtId.setEditable(false);
    }

    private void agregarFila(Servicio s) {
        modeloTabla.addRow(new Object[]{s.getIdServicio(), s.getDescripcion(), s.getCostoManoObra()});
    }
}
