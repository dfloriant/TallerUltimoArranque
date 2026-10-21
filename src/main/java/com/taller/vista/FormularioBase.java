package com.taller.vista;

import com.taller.excepciones.ValidacionException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public abstract class FormularioBase extends JFrame {

    protected interface Accion {
        void ejecutar() throws SQLException, ValidacionException;
    }

    private final JPanel panelCampos = new JPanel(new GridBagLayout());
    private final JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private int totalCampos = 0;
    protected final DefaultTableModel modeloTabla;
    protected final JTable tabla;

    protected FormularioBase(String titulo, String[] columnas) {
        setTitle(titulo);
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                alSeleccionarFila(tabla.getSelectedRow());
            }
        });

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBorder(new EmptyBorder(10, 10, 10, 10));
        norte.add(panelCampos, BorderLayout.CENTER);
        norte.add(panelBotones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        agregarBoton("Guardar", e -> ejecutar(this::guardar));
        agregarBoton("Modificar", e -> ejecutar(this::modificar));
        agregarBoton("Eliminar", e -> ejecutar(this::eliminar));
        agregarBoton("Buscar", e -> ejecutar(this::buscar));
        agregarBoton("Limpiar", e -> ejecutar(this::limpiar));
    }

    protected abstract void guardar() throws SQLException, ValidacionException;

    protected abstract void modificar() throws SQLException, ValidacionException;

    protected abstract void eliminar() throws SQLException, ValidacionException;

    protected abstract void buscar() throws SQLException, ValidacionException;

    protected abstract void limpiar() throws SQLException, ValidacionException;

    protected abstract void cargarTabla() throws SQLException, ValidacionException;

    protected abstract void alSeleccionarFila(int fila);

    protected void agregarCampo(String etiqueta, JComponent campo) {
        int fila = totalCampos / 2;
        int columna = (totalCampos % 2) * 2;
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.gridy = fila;
        g.gridx = columna;
        g.anchor = GridBagConstraints.WEST;
        panelCampos.add(new JLabel(etiqueta), g);
        g.gridx = columna + 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        panelCampos.add(campo, g);
        totalCampos++;
    }

    protected JButton agregarBoton(String texto, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(accion);
        panelBotones.add(boton);
        return boton;
    }

    protected void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected String leer(JTextField campo, String nombre, boolean obligatorio) throws ValidacionException {
        String valor = campo.getText().trim();
        if (obligatorio && valor.isEmpty()) {
            campo.requestFocus();
            throw new ValidacionException("El campo " + nombre + " es obligatorio.");
        }
        return valor.isEmpty() ? null : valor;
    }

    protected int leerEntero(JTextField campo, String nombre, int minimo, int maximo) throws ValidacionException {
        String valor = leer(campo, nombre, true);
        try {
            int numero = Integer.parseInt(valor);
            if (numero < minimo || numero > maximo) {
                throw new ValidacionException("El campo " + nombre + " debe estar entre " + minimo + " y " + maximo + ".");
            }
            return numero;
        } catch (NumberFormatException e) {
            campo.requestFocus();
            throw new ValidacionException("El campo " + nombre + " debe ser un número entero.");
        }
    }

    protected BigDecimal leerDecimal(JTextField campo, String nombre) throws ValidacionException {
        String valor = leer(campo, nombre, true);
        try {
            BigDecimal numero = new BigDecimal(valor);
            if (numero.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidacionException("El campo " + nombre + " no puede ser negativo.");
            }
            if (numero.scale() > 2) {
                throw new ValidacionException("El campo " + nombre + " admite como máximo 2 decimales.");
            }
            return numero;
        } catch (NumberFormatException e) {
            campo.requestFocus();
            throw new ValidacionException("El campo " + nombre + " debe ser un número válido (ejemplo: 125.50).");
        }
    }

    protected void validarTelefono(String telefono) throws ValidacionException {
        if (telefono != null && !telefono.matches("\\d{8}")) {
            throw new ValidacionException("El teléfono debe tener 8 dígitos.");
        }
    }

    protected boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(this, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    protected void informar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    protected String valorTabla(int fila, int columna) {
        Object valor = modeloTabla.getValueAt(fila, columna);
        return valor == null ? "" : valor.toString();
    }
}
