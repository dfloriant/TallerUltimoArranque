package com.taller.vista;

import com.taller.dao.RepuestoDAO;
import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Repuesto;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class FrmRepuestos extends FormularioBase {

    private enum Filtro { TODOS, AGOTADOS, BAJO_INVENTARIO }

    private final RepuestoDAO dao = new RepuestoDAO();
    private final JTextField txtId = new JTextField(15);
    private final JTextField txtNombre = new JTextField(15);
    private final JTextField txtDescripcion = new JTextField(15);
    private final JTextField txtPrecio = new JTextField(15);
    private final JTextField txtExistencia = new JTextField(15);
    private Filtro filtro = Filtro.TODOS;

    public FrmRepuestos() {
        super("Gestión de Repuestos", new String[]{"Identificador", "Nombre", "Descripción", "Precio (Q)", "Existencia", "Estado"});
        agregarCampo("Identificador:", txtId);
        agregarCampo("Nombre:", txtNombre);
        agregarCampo("Descripción:", txtDescripcion);
        agregarCampo("Precio (Q):", txtPrecio);
        agregarCampo("Existencia inicial:", txtExistencia);
        agregarBoton("Abastecer", e -> ejecutar(this::abastecer));
        agregarBoton("Ver agotados", e -> ejecutar(() -> cambiarFiltro(Filtro.AGOTADOS)));
        agregarBoton("Bajo inventario", e -> ejecutar(() -> cambiarFiltro(Filtro.BAJO_INVENTARIO)));
        agregarBoton("Ver todos", e -> ejecutar(() -> cambiarFiltro(Filtro.TODOS)));
        agregarBoton("Historial abastecimiento", e -> ejecutar(this::verHistorial));
        ejecutar(this::cargarTabla);
    }

    private Repuesto leerRepuesto(boolean nuevo) throws ValidacionException {
        String id = leer(txtId, "Identificador", true).toUpperCase();
        String nombre = leer(txtNombre, "Nombre", true);
        String descripcion = leer(txtDescripcion, "Descripción", false);
        int existencia = nuevo ? leerEntero(txtExistencia, "Existencia inicial", 0, 99999999) : 0;
        return new Repuesto(id, nombre, descripcion, leerDecimal(txtPrecio, "Precio"), existencia);
    }

    @Override
    protected void guardar() throws SQLException, ValidacionException {
        dao.insertar(leerRepuesto(true));
        informar("Repuesto registrado correctamente.");
        limpiar();
    }

    @Override
    protected void modificar() throws SQLException, ValidacionException {
        Repuesto repuesto = leerRepuesto(false);
        if (dao.buscarPorId(repuesto.getIdRepuesto()) == null) {
            throw new ValidacionException("Seleccione un repuesto existente para modificar.");
        }
        dao.actualizar(repuesto);
        informar("Repuesto modificado correctamente. La existencia solo cambia con Abastecer o con las órdenes.");
        limpiar();
    }

    @Override
    protected void eliminar() throws SQLException, ValidacionException {
        String id = leer(txtId, "Identificador", true).toUpperCase();
        if (confirmar("¿Desea eliminar el repuesto " + id + "?")) {
            dao.eliminar(id);
            informar("Repuesto eliminado correctamente.");
            limpiar();
        }
    }

    @Override
    protected void buscar() throws SQLException, ValidacionException {
        String id = leer(txtId, "Identificador", true).toUpperCase();
        Repuesto repuesto = dao.buscarPorId(id);
        if (repuesto == null) {
            throw new ValidacionException("No existe un repuesto con el identificador " + id + ".");
        }
        modeloTabla.setRowCount(0);
        agregarFila(repuesto);
        tabla.setRowSelectionInterval(0, 0);
    }

    @Override
    protected void limpiar() throws SQLException, ValidacionException {
        txtId.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        txtExistencia.setText("");
        txtId.setEditable(true);
        txtExistencia.setEditable(true);
        tabla.clearSelection();
        cargarTabla();
    }

    @Override
    protected void cargarTabla() throws SQLException {
        ArrayList<Repuesto> lista = switch (filtro) {
            case AGOTADOS -> dao.listarAgotados();
            case BAJO_INVENTARIO -> dao.listarBajoInventario();
            default -> dao.listar();
        };
        modeloTabla.setRowCount(0);
        for (Repuesto r : lista) {
            agregarFila(r);
        }
    }

    @Override
    protected void alSeleccionarFila(int fila) {
        txtId.setText(valorTabla(fila, 0));
        txtNombre.setText(valorTabla(fila, 1));
        txtDescripcion.setText(valorTabla(fila, 2));
        txtPrecio.setText(valorTabla(fila, 3));
        txtExistencia.setText(valorTabla(fila, 4));
        txtId.setEditable(false);
        txtExistencia.setEditable(false);
    }

    private void agregarFila(Repuesto r) {
        String estado;
        if (r.estaAgotado()) {
            estado = "AGOTADO";
        } else if (r.getExistencia() <= RepuestoDAO.LIMITE_BAJO_INVENTARIO) {
            estado = "BAJO";
        } else {
            estado = "DISPONIBLE";
        }
        modeloTabla.addRow(new Object[]{r.getIdRepuesto(), r.getNombre(), r.getDescripcion(), r.getPrecio(), r.getExistencia(), estado});
    }

    private void cambiarFiltro(Filtro nuevo) throws SQLException {
        filtro = nuevo;
        cargarTabla();
        if (modeloTabla.getRowCount() == 0 && nuevo != Filtro.TODOS) {
            informar("No hay repuestos en esta categoría.");
        }
    }

    private void abastecer() throws SQLException, ValidacionException {
        String id = leer(txtId, "Identificador", true).toUpperCase();
        Repuesto repuesto = dao.buscarPorId(id);
        if (repuesto == null) {
            throw new ValidacionException("Seleccione un repuesto existente para abastecer.");
        }
        String valor = JOptionPane.showInputDialog(this,
                "Repuesto: " + repuesto.getNombre() + "\nExistencia actual: " + repuesto.getExistencia() + "\n\nCantidad a ingresar:",
                "Abastecer inventario", JOptionPane.QUESTION_MESSAGE);
        if (valor == null) {
            return;
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new ValidacionException("La cantidad debe ser un número entero.");
        }
        dao.abastecer(id, cantidad);
        informar("Se agregaron " + cantidad + " unidades. Nueva existencia: " + (repuesto.getExistencia() + cantidad));
        limpiar();
    }

    private void verHistorial() throws SQLException, ValidacionException {
        String id = leer(txtId, "Identificador", true).toUpperCase();
        ArrayList<Object[]> filas = dao.historialAbastecimiento(id);
        if (filas.isEmpty()) {
            informar("El repuesto " + id + " no tiene abastecimientos registrados.");
            return;
        }
        DefaultTableModel modelo = new DefaultTableModel(new String[]{"Fecha", "Cantidad ingresada"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
        JDialog dialogo = new JDialog(this, "Historial de abastecimiento - " + id, true);
        dialogo.add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);
        dialogo.setSize(400, 300);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }
}
