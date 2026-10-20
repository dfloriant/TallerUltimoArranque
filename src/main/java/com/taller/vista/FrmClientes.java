package com.taller.vista;

import com.taller.dao.ClienteDAO;
import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Cliente;
import java.sql.SQLException;
import javax.swing.JTextField;

public class FrmClientes extends FormularioBase {

    private final ClienteDAO dao = new ClienteDAO();
    private final JTextField txtId = new JTextField(15);
    private final JTextField txtNombre = new JTextField(15);
    private final JTextField txtApellido = new JTextField(15);
    private final JTextField txtTelefono = new JTextField(15);
    private final JTextField txtDireccion = new JTextField(15);
    private final JTextField txtCorreo = new JTextField(15);

    public FrmClientes() {
        super("Gestión de Clientes", new String[]{"Identificador", "Nombre", "Apellido", "Teléfono", "Dirección", "Correo"});
        agregarCampo("Identificador (DPI/NIT):", txtId);
        agregarCampo("Nombre:", txtNombre);
        agregarCampo("Apellido:", txtApellido);
        agregarCampo("Teléfono:", txtTelefono);
        agregarCampo("Dirección:", txtDireccion);
        agregarCampo("Correo:", txtCorreo);
        agregarBoton("Ver vehículos", e -> verVehiculos());
        ejecutar(this::cargarTabla);
    }

    private Cliente leerCliente() throws ValidacionException {
        String id = leer(txtId, "Identificador", true);
        String nombre = leer(txtNombre, "Nombre", true);
        String apellido = leer(txtApellido, "Apellido", true);
        String telefono = leer(txtTelefono, "Teléfono", false);
        String direccion = leer(txtDireccion, "Dirección", false);
        String correo = leer(txtCorreo, "Correo", false);
        validarTelefono(telefono);
        if (correo != null && !correo.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            throw new ValidacionException("El correo no tiene un formato válido.");
        }
        return new Cliente(id, nombre, apellido, telefono, direccion, correo);
    }

    @Override
    protected void guardar() throws SQLException, ValidacionException {
        dao.insertar(leerCliente());
        informar("Cliente registrado correctamente.");
        limpiar();
    }

    @Override
    protected void modificar() throws SQLException, ValidacionException {
        Cliente cliente = leerCliente();
        if (dao.buscarPorId(cliente.getId()) == null) {
            throw new ValidacionException("Seleccione un cliente existente para modificar.");
        }
        dao.actualizar(cliente);
        informar("Cliente modificado correctamente.");
        limpiar();
    }

    @Override
    protected void eliminar() throws SQLException, ValidacionException {
        String id = leer(txtId, "Identificador", true);
        if (confirmar("¿Desea eliminar el cliente " + id + "?")) {
            dao.eliminar(id);
            informar("Cliente eliminado correctamente.");
            limpiar();
        }
    }

    @Override
    protected void buscar() throws SQLException, ValidacionException {
        String id = leer(txtId, "Identificador", true);
        Cliente cliente = dao.buscarPorId(id);
        if (cliente == null) {
            throw new ValidacionException("No existe un cliente con el identificador " + id + ".");
        }
        modeloTabla.setRowCount(0);
        agregarFila(cliente);
        tabla.setRowSelectionInterval(0, 0);
    }

    @Override
    protected void limpiar() throws SQLException, ValidacionException {
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        txtCorreo.setText("");
        txtId.setEditable(true);
        tabla.clearSelection();
        cargarTabla();
    }

    @Override
    protected void cargarTabla() throws SQLException {
        modeloTabla.setRowCount(0);
        for (Cliente c : dao.listar()) {
            agregarFila(c);
        }
    }

    @Override
    protected void alSeleccionarFila(int fila) {
        txtId.setText(valorTabla(fila, 0));
        txtNombre.setText(valorTabla(fila, 1));
        txtApellido.setText(valorTabla(fila, 2));
        txtTelefono.setText(valorTabla(fila, 3));
        txtDireccion.setText(valorTabla(fila, 4));
        txtCorreo.setText(valorTabla(fila, 5));
        txtId.setEditable(false);
    }

    private void agregarFila(Cliente c) {
        modeloTabla.addRow(new Object[]{c.getId(), c.getNombre(), c.getApellido(), c.getTelefono(), c.getDireccion(), c.getCorreo()});
    }

    private void verVehiculos() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            informar("Seleccione un cliente de la tabla.");
            return;
        }
        new FrmVehiculos(id).setVisible(true);
    }
}
