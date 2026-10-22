package com.taller.vista;

import com.taller.dao.ClienteDAO;
import com.taller.dao.MecanicoDAO;
import com.taller.dao.OrdenDAO;
import com.taller.dao.VehiculoDAO;
import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Cliente;
import com.taller.modelo.Mecanico;
import com.taller.modelo.OrdenTrabajo;
import com.taller.modelo.Vehiculo;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class FrmRecepcion extends JFrame {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private final MecanicoDAO mecanicoDAO = new MecanicoDAO();
    private final OrdenDAO ordenDAO = new OrdenDAO();

    private final JTextField txtBuscar = new JTextField(20);
    private final JComboBox<Cliente> cboCliente = new JComboBox<>();
    private final JComboBox<Vehiculo> cboVehiculo = new JComboBox<>();
    private final JComboBox<Mecanico> cboMecanico = new JComboBox<>();
    private final JTextArea txtProblema = new JTextArea(5, 30);
    private final JLabel lblAviso = new JLabel(" ");
    private final ArrayList<Cliente> todosLosClientes = new ArrayList<>();

    public FrmRecepcion() {
        setTitle("Recepción de vehículo");
        setSize(700, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        txtProblema.setLineWrap(true);
        txtProblema.setWrapStyleWord(true);
        lblAviso.setForeground(new Color(180, 90, 0));

        JButton btnBuscar = new JButton("Buscar");
        JButton btnNuevoCliente = new JButton("Nuevo cliente");
        JButton btnNuevoVehiculo = new JButton("Nuevo vehículo");
        JButton btnGenerar = new JButton("Generar orden de trabajo");

        fila(panel, 0, "Buscar cliente (ID o nombre):", txtBuscar, btnBuscar);
        fila(panel, 1, "Cliente:", cboCliente, btnNuevoCliente);
        fila(panel, 2, "Vehículo:", cboVehiculo, btnNuevoVehiculo);
        fila(panel, 3, "", lblAviso, null);
        fila(panel, 4, "Mecánico asignado:", cboMecanico, null);
        fila(panel, 5, "Problema reportado:", new JScrollPane(txtProblema), null);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = 6;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(12, 6, 6, 6);
        panel.add(btnGenerar, g);
        add(panel);

        btnBuscar.addActionListener(e -> Ui.ejecutar(this, this::buscarCliente));
        txtBuscar.addActionListener(e -> Ui.ejecutar(this, this::buscarCliente));
        cboCliente.addActionListener(e -> Ui.ejecutar(this, this::cargarVehiculos));
        btnNuevoCliente.addActionListener(e -> abrirYRecargar(new FrmClientes(), this::cargarClientes));
        btnNuevoVehiculo.addActionListener(e -> nuevoVehiculo());
        btnGenerar.addActionListener(e -> Ui.ejecutar(this, this::generarOrden));

        Ui.ejecutar(this, this::cargarClientes);
        Ui.ejecutar(this, this::cargarMecanicos);
    }

    private void fila(JPanel panel, int fila, String etiqueta, JComponent campo, JComponent extra) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.gridy = fila;
        g.gridx = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(etiqueta), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        panel.add(campo, g);
        if (extra != null) {
            g.gridx = 2;
            g.weightx = 0;
            panel.add(extra, g);
        }
    }

    private void cargarClientes() throws SQLException {
        Cliente seleccionado = (Cliente) cboCliente.getSelectedItem();
        todosLosClientes.clear();
        todosLosClientes.addAll(clienteDAO.listar());
        mostrarClientes(todosLosClientes);
        if (seleccionado != null) {
            for (int i = 0; i < cboCliente.getItemCount(); i++) {
                if (cboCliente.getItemAt(i).getId().equals(seleccionado.getId())) {
                    cboCliente.setSelectedIndex(i);
                }
            }
        }
    }

    private void mostrarClientes(ArrayList<Cliente> lista) {
        cboCliente.removeAllItems();
        for (Cliente c : lista) {
            cboCliente.addItem(c);
        }
    }

    private void cargarMecanicos() throws SQLException {
        cboMecanico.removeAllItems();
        for (Mecanico m : mecanicoDAO.listar()) {
            cboMecanico.addItem(m);
        }
    }

    private void cargarVehiculos() throws SQLException {
        cboVehiculo.removeAllItems();
        Cliente cliente = (Cliente) cboCliente.getSelectedItem();
        if (cliente == null) {
            lblAviso.setText(" ");
            return;
        }
        for (Vehiculo v : vehiculoDAO.listarPorCliente(cliente.getId())) {
            cliente.agregarVehiculo(v);
            cboVehiculo.addItem(v);
        }
        lblAviso.setText(cboVehiculo.getItemCount() == 0
                ? "El cliente no tiene vehículos. Use \"Nuevo vehículo\" para registrarlo."
                : " ");
    }

    private void buscarCliente() throws SQLException, ValidacionException {
        String texto = txtBuscar.getText().trim().toLowerCase();
        if (texto.isEmpty()) {
            mostrarClientes(todosLosClientes);
            return;
        }
        ArrayList<Cliente> encontrados = new ArrayList<>();
        for (Cliente c : todosLosClientes) {
            if (c.getId().toLowerCase().contains(texto) || c.getNombreCompleto().toLowerCase().contains(texto)) {
                encontrados.add(c);
            }
        }
        if (encontrados.isEmpty()) {
            if (Ui.confirmar(this, "No se encontró ningún cliente con \"" + txtBuscar.getText().trim() + "\".\n¿Desea registrarlo ahora?")) {
                abrirYRecargar(new FrmClientes(), this::cargarClientes);
            }
            return;
        }
        mostrarClientes(encontrados);
    }

    private void nuevoVehiculo() {
        Cliente cliente = (Cliente) cboCliente.getSelectedItem();
        if (cliente == null) {
            Ui.informar(this, "Primero seleccione o registre un cliente.");
            return;
        }
        abrirYRecargar(new FrmVehiculos(cliente.getId()), this::cargarVehiculos);
    }

    private void abrirYRecargar(JFrame ventana, Ui.Accion alCerrar) {
        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Ui.ejecutar(FrmRecepcion.this, alCerrar);
            }
        });
        ventana.setVisible(true);
    }

    private void generarOrden() throws SQLException, ValidacionException {
        Vehiculo vehiculo = (Vehiculo) cboVehiculo.getSelectedItem();
        Mecanico mecanico = (Mecanico) cboMecanico.getSelectedItem();
        if (vehiculo == null) {
            throw new ValidacionException("Seleccione un vehículo. Si el cliente no tiene, regístrelo con \"Nuevo vehículo\".");
        }
        if (mecanico == null) {
            throw new ValidacionException("Seleccione el mecánico que atenderá la orden.");
        }
        String problema = txtProblema.getText().trim();
        if (problema.isEmpty()) {
            txtProblema.requestFocus();
            throw new ValidacionException("Ingrese la descripción del problema reportado.");
        }
        OrdenTrabajo orden = new OrdenTrabajo(vehiculo, mecanico, problema);
        int id = ordenDAO.crear(orden);
        Ui.informar(this, "Orden de trabajo #" + id + " creada en estado " + orden.getEstado() + ".\n"
                + "Vehículo: " + vehiculo + "\nMecánico: " + mecanico.getNombreCompleto());
        txtProblema.setText("");
    }
}
