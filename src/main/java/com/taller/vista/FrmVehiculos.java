package com.taller.vista;

import com.taller.dao.ClienteDAO;
import com.taller.dao.VehiculoDAO;
import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Cliente;
import com.taller.modelo.Vehiculo;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class FrmVehiculos extends FormularioBase {

    private final VehiculoDAO dao = new VehiculoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final JTextField txtPlaca = new JTextField(15);
    private final JTextField txtMarca = new JTextField(15);
    private final JTextField txtModelo = new JTextField(15);
    private final JTextField txtAnio = new JTextField(15);
    private final JTextField txtColor = new JTextField(15);
    private final JComboBox<Cliente> cboCliente = new JComboBox<>();
    private final ArrayList<Vehiculo> vehiculosEnTabla = new ArrayList<>();
    private String idClienteFiltro;

    public FrmVehiculos() {
        this(null);
    }

    public FrmVehiculos(String idClienteFiltro) {
        super("Gestión de Vehículos", new String[]{"Placa", "Marca", "Modelo", "Año", "Color", "Cliente"});
        this.idClienteFiltro = idClienteFiltro;
        agregarCampo("Placa:", txtPlaca);
        agregarCampo("Marca:", txtMarca);
        agregarCampo("Modelo:", txtModelo);
        agregarCampo("Año:", txtAnio);
        agregarCampo("Color:", txtColor);
        agregarCampo("Cliente:", cboCliente);
        agregarBoton("Ver por cliente", e -> ejecutar(this::filtrarPorCliente));
        agregarBoton("Ver todos", e -> ejecutar(this::verTodos));
        agregarBoton("Historial de órdenes", e -> ejecutar(this::verHistorial));
        ejecutar(this::cargarClientes);
        ejecutar(this::cargarTabla);
    }

    private void cargarClientes() throws SQLException {
        cboCliente.removeAllItems();
        for (Cliente c : clienteDAO.listar()) {
            cboCliente.addItem(c);
        }
        if (idClienteFiltro != null) {
            seleccionarCliente(idClienteFiltro);
        }
    }

    private void seleccionarCliente(String idCliente) {
        for (int i = 0; i < cboCliente.getItemCount(); i++) {
            if (cboCliente.getItemAt(i).getId().equals(idCliente)) {
                cboCliente.setSelectedIndex(i);
                return;
            }
        }
    }

    private Vehiculo leerVehiculo() throws ValidacionException {
        String placa = leer(txtPlaca, "Placa", true).toUpperCase();
        if (!placa.matches("[A-Z0-9]{5,10}")) {
            throw new ValidacionException("La placa solo puede tener letras y números (de 5 a 10 caracteres).");
        }
        String marca = leer(txtMarca, "Marca", true);
        String modelo = leer(txtModelo, "Modelo", true);
        int anio = leerEntero(txtAnio, "Año", 1950, Year.now().getValue() + 1);
        String color = leer(txtColor, "Color", false);
        Cliente cliente = (Cliente) cboCliente.getSelectedItem();
        if (cliente == null) {
            throw new ValidacionException("Debe registrar primero al cliente y seleccionarlo.");
        }
        return new Vehiculo(placa, marca, modelo, anio, color, cliente);
    }

    @Override
    protected void guardar() throws SQLException, ValidacionException {
        dao.insertar(leerVehiculo());
        informar("Vehículo registrado correctamente.");
        limpiar();
    }

    @Override
    protected void modificar() throws SQLException, ValidacionException {
        Vehiculo vehiculo = leerVehiculo();
        if (dao.buscarPorPlaca(vehiculo.getPlaca()) == null) {
            throw new ValidacionException("Seleccione un vehículo existente para modificar.");
        }
        dao.actualizar(vehiculo);
        informar("Vehículo modificado correctamente.");
        limpiar();
    }

    @Override
    protected void eliminar() throws SQLException, ValidacionException {
        String placa = leer(txtPlaca, "Placa", true).toUpperCase();
        if (confirmar("¿Desea eliminar el vehículo " + placa + "?")) {
            dao.eliminar(placa);
            informar("Vehículo eliminado correctamente.");
            limpiar();
        }
    }

    @Override
    protected void buscar() throws SQLException, ValidacionException {
        String placa = leer(txtPlaca, "Placa", true).toUpperCase();
        Vehiculo vehiculo = dao.buscarPorPlaca(placa);
        if (vehiculo == null) {
            throw new ValidacionException("No existe un vehículo con la placa " + placa + ".");
        }
        ArrayList<Vehiculo> lista = new ArrayList<>();
        lista.add(vehiculo);
        mostrar(lista);
        tabla.setRowSelectionInterval(0, 0);
    }

    @Override
    protected void limpiar() throws SQLException, ValidacionException {
        txtPlaca.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtAnio.setText("");
        txtColor.setText("");
        txtPlaca.setEditable(true);
        tabla.clearSelection();
        cargarClientes();
        cargarTabla();
    }

    @Override
    protected void cargarTabla() throws SQLException {
        if (idClienteFiltro == null) {
            mostrar(dao.listar());
        } else {
            mostrar(dao.listarPorCliente(idClienteFiltro));
        }
    }

    private void mostrar(ArrayList<Vehiculo> lista) {
        vehiculosEnTabla.clear();
        vehiculosEnTabla.addAll(lista);
        modeloTabla.setRowCount(0);
        for (Vehiculo v : lista) {
            modeloTabla.addRow(new Object[]{v.getPlaca(), v.getMarca(), v.getModelo(), v.getAnio(), v.getColor(), v.getCliente()});
        }
    }

    @Override
    protected void alSeleccionarFila(int fila) {
        Vehiculo v = vehiculosEnTabla.get(fila);
        txtPlaca.setText(v.getPlaca());
        txtMarca.setText(v.getMarca());
        txtModelo.setText(v.getModelo());
        txtAnio.setText(String.valueOf(v.getAnio()));
        txtColor.setText(v.getColor() == null ? "" : v.getColor());
        seleccionarCliente(v.getCliente().getId());
        txtPlaca.setEditable(false);
    }

    private void filtrarPorCliente() throws SQLException, ValidacionException {
        Cliente cliente = (Cliente) cboCliente.getSelectedItem();
        if (cliente == null) {
            throw new ValidacionException("Seleccione un cliente.");
        }
        idClienteFiltro = cliente.getId();
        cargarTabla();
        if (vehiculosEnTabla.isEmpty()) {
            informar("El cliente " + cliente.getNombreCompleto() + " no tiene vehículos registrados.");
        }
    }

    private void verTodos() throws SQLException {
        idClienteFiltro = null;
        cargarTabla();
    }

    private void verHistorial() throws SQLException, ValidacionException {
        String placa = leer(txtPlaca, "Placa", true).toUpperCase();
        ArrayList<Object[]> filas = dao.historialOrdenes(placa);
        if (filas.isEmpty()) {
            informar("El vehículo " + placa + " no tiene órdenes de trabajo registradas.");
            return;
        }
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"Orden", "Fecha", "Mecánico", "Problema", "Estado", "Total (Q)"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
        JDialog dialogo = new JDialog(this, "Historial de órdenes - " + placa, true);
        dialogo.add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);
        dialogo.setSize(800, 300);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }
}
