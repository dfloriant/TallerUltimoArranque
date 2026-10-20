package com.taller.vista;

import com.taller.dao.MecanicoDAO;
import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Mecanico;
import java.sql.SQLException;
import javax.swing.JTextField;

public class FrmMecanicos extends FormularioBase {

    private final MecanicoDAO dao = new MecanicoDAO();
    private final JTextField txtId = new JTextField(15);
    private final JTextField txtNombre = new JTextField(15);
    private final JTextField txtApellido = new JTextField(15);
    private final JTextField txtTelefono = new JTextField(15);
    private final JTextField txtEspecialidad = new JTextField(15);

    public FrmMecanicos() {
        super("Gestión de Mecánicos", new String[]{"Código", "Nombre", "Apellido", "Teléfono", "Especialidad"});
        agregarCampo("Código:", txtId);
        agregarCampo("Nombre:", txtNombre);
        agregarCampo("Apellido:", txtApellido);
        agregarCampo("Teléfono:", txtTelefono);
        agregarCampo("Especialidad:", txtEspecialidad);
        ejecutar(this::cargarTabla);
    }

    private Mecanico leerMecanico() throws ValidacionException {
        String id = leer(txtId, "Código", true);
        String nombre = leer(txtNombre, "Nombre", true);
        String apellido = leer(txtApellido, "Apellido", true);
        String telefono = leer(txtTelefono, "Teléfono", false);
        String especialidad = leer(txtEspecialidad, "Especialidad", false);
        validarTelefono(telefono);
        return new Mecanico(id, nombre, apellido, telefono, especialidad);
    }

    @Override
    protected void guardar() throws SQLException, ValidacionException {
        dao.insertar(leerMecanico());
        informar("Mecánico registrado correctamente.");
        limpiar();
    }

    @Override
    protected void modificar() throws SQLException, ValidacionException {
        Mecanico mecanico = leerMecanico();
        if (dao.buscarPorId(mecanico.getId()) == null) {
            throw new ValidacionException("Seleccione un mecánico existente para modificar.");
        }
        dao.actualizar(mecanico);
        informar("Mecánico modificado correctamente.");
        limpiar();
    }

    @Override
    protected void eliminar() throws SQLException, ValidacionException {
        String id = leer(txtId, "Código", true);
        if (confirmar("¿Desea eliminar el mecánico " + id + "?")) {
            dao.eliminar(id);
            informar("Mecánico eliminado correctamente.");
            limpiar();
        }
    }

    @Override
    protected void buscar() throws SQLException, ValidacionException {
        String id = leer(txtId, "Código", true);
        Mecanico mecanico = dao.buscarPorId(id);
        if (mecanico == null) {
            throw new ValidacionException("No existe un mecánico con el código " + id + ".");
        }
        modeloTabla.setRowCount(0);
        agregarFila(mecanico);
        tabla.setRowSelectionInterval(0, 0);
    }

    @Override
    protected void limpiar() throws SQLException, ValidacionException {
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        txtEspecialidad.setText("");
        txtId.setEditable(true);
        tabla.clearSelection();
        cargarTabla();
    }

    @Override
    protected void cargarTabla() throws SQLException {
        modeloTabla.setRowCount(0);
        for (Mecanico m : dao.listar()) {
            agregarFila(m);
        }
    }

    @Override
    protected void alSeleccionarFila(int fila) {
        txtId.setText(valorTabla(fila, 0));
        txtNombre.setText(valorTabla(fila, 1));
        txtApellido.setText(valorTabla(fila, 2));
        txtTelefono.setText(valorTabla(fila, 3));
        txtEspecialidad.setText(valorTabla(fila, 4));
        txtId.setEditable(false);
    }

    private void agregarFila(Mecanico m) {
        modeloTabla.addRow(new Object[]{m.getId(), m.getNombre(), m.getApellido(), m.getTelefono(), m.getEspecialidad()});
    }
}
