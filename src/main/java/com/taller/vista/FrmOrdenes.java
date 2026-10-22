package com.taller.vista;

import com.taller.dao.OrdenDAO;
import com.taller.dao.RepuestoDAO;
import com.taller.dao.ServicioDAO;
import com.taller.modelo.DetalleRepuesto;
import com.taller.modelo.DetalleServicio;
import com.taller.modelo.EstadoOrden;
import com.taller.modelo.OrdenTrabajo;
import com.taller.modelo.Repuesto;
import com.taller.modelo.Servicio;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class FrmOrdenes extends JFrame {

    private final OrdenDAO ordenDAO = new OrdenDAO();
    private final ServicioDAO servicioDAO = new ServicioDAO();
    private final RepuestoDAO repuestoDAO = new RepuestoDAO();

    private final JComboBox<Object> cboEstado = new JComboBox<>();
    private final DefaultTableModel modeloOrdenes = Ui.modeloTabla("Orden", "Fecha", "Placa", "Cliente", "Mecánico", "Estado", "Total");
    private final JTable tblOrdenes = new JTable(modeloOrdenes);
    private final DefaultTableModel modeloServicios = Ui.modeloTabla("Servicio", "Costo");
    private final JTable tblServicios = new JTable(modeloServicios);
    private final DefaultTableModel modeloRepuestos = Ui.modeloTabla("Repuesto", "Cantidad", "Precio unitario", "Subtotal");
    private final JTable tblRepuestos = new JTable(modeloRepuestos);
    private final JLabel lblInfo = new JLabel("Seleccione una orden");
    private final JLabel lblTotales = new JLabel(" ");
    private final JComboBox<Servicio> cboServicio = new JComboBox<>();
    private final JComboBox<Repuesto> cboRepuesto = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

    private final JButton btnIniciar = new JButton("Iniciar reparación");
    private final JButton btnFinalizar = new JButton("Finalizar orden");
    private final JButton btnCancelar = new JButton("Cancelar orden");
    private final JButton btnComprobante = new JButton("Ver comprobante");
    private final JButton btnAgregarServicio = new JButton("Agregar servicio");
    private final JButton btnQuitarServicio = new JButton("Quitar servicio");
    private final JButton btnAgregarRepuesto = new JButton("Agregar repuesto");
    private final JButton btnQuitarRepuesto = new JButton("Quitar repuesto");

    private OrdenTrabajo ordenActual;

    public FrmOrdenes() {
        setTitle("Gestión de órdenes de trabajo");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cboEstado.addItem("Todos");
        for (EstadoOrden estado : EstadoOrden.values()) {
            cboEstado.addItem(estado);
        }
        JButton btnActualizar = new JButton("Actualizar");
        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        norte.add(new JLabel("Estado:"));
        norte.add(cboEstado);
        norte.add(btnActualizar);

        tblOrdenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblServicios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRepuestos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tblOrdenes), crearPanelDetalle());
        split.setResizeWeight(0.45);

        add(norte, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        cboEstado.addActionListener(e -> cargarOrdenes(0));
        btnActualizar.addActionListener(e -> cargarOrdenes(idSeleccionado()));
        tblOrdenes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle(idSeleccionado());
            }
        });
        btnIniciar.addActionListener(e -> accionSobreOrden(() -> ordenDAO.iniciar(ordenActual.getIdOrden())));
        btnAgregarServicio.addActionListener(e -> agregarServicio());
        btnQuitarServicio.addActionListener(e -> quitarServicio());
        btnAgregarRepuesto.addActionListener(e -> agregarRepuesto());
        btnQuitarRepuesto.addActionListener(e -> quitarRepuesto());
        btnFinalizar.addActionListener(e -> finalizar());
        btnCancelar.addActionListener(e -> cancelar());
        btnComprobante.addActionListener(e -> verComprobante());

        cargarCatalogos();
        cargarOrdenes(0);
    }

    private JPanel crearPanelDetalle() {
        JPanel detalle = new JPanel(new BorderLayout(5, 5));
        detalle.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        lblInfo.setVerticalAlignment(JLabel.TOP);
        detalle.add(lblInfo, BorderLayout.NORTH);

        JPanel panelServicios = new JPanel(new BorderLayout());
        panelServicios.setBorder(BorderFactory.createTitledBorder("Servicios realizados"));
        panelServicios.add(new JScrollPane(tblServicios), BorderLayout.CENTER);
        JPanel accionesServicio = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accionesServicio.add(cboServicio);
        accionesServicio.add(btnAgregarServicio);
        accionesServicio.add(btnQuitarServicio);
        panelServicios.add(accionesServicio, BorderLayout.SOUTH);

        JPanel panelRepuestos = new JPanel(new BorderLayout());
        panelRepuestos.setBorder(BorderFactory.createTitledBorder("Repuestos utilizados"));
        panelRepuestos.add(new JScrollPane(tblRepuestos), BorderLayout.CENTER);
        JPanel accionesRepuesto = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accionesRepuesto.add(cboRepuesto);
        accionesRepuesto.add(new JLabel("Cantidad:"));
        accionesRepuesto.add(spnCantidad);
        accionesRepuesto.add(btnAgregarRepuesto);
        accionesRepuesto.add(btnQuitarRepuesto);
        panelRepuestos.add(accionesRepuesto, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new GridLayout(2, 1, 5, 5));
        centro.add(panelServicios);
        centro.add(panelRepuestos);
        detalle.add(centro, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnIniciar);
        botones.add(btnFinalizar);
        botones.add(btnCancelar);
        botones.add(btnComprobante);
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(lblTotales, BorderLayout.NORTH);
        sur.add(botones, BorderLayout.SOUTH);
        detalle.add(sur, BorderLayout.SOUTH);
        return detalle;
    }

    private void cargarCatalogos() {
        Ui.ejecutar(this, () -> {
            cboServicio.removeAllItems();
            for (Servicio s : servicioDAO.listar()) {
                cboServicio.addItem(s);
            }
            cargarRepuestos();
        });
    }

    private void cargarRepuestos() throws SQLException {
        cboRepuesto.removeAllItems();
        for (Repuesto r : repuestoDAO.listar()) {
            cboRepuesto.addItem(r);
        }
    }

    private int idSeleccionado() {
        int fila = tblOrdenes.getSelectedRow();
        return fila < 0 ? 0 : (Integer) modeloOrdenes.getValueAt(fila, 0);
    }

    private void cargarOrdenes(int idSeleccionar) {
        Ui.ejecutar(this, () -> {
            Object filtro = cboEstado.getSelectedItem();
            EstadoOrden estado = filtro instanceof EstadoOrden ? (EstadoOrden) filtro : null;
            modeloOrdenes.setRowCount(0);
            for (Object[] fila : ordenDAO.listar(estado)) {
                fila[6] = Ui.moneda((BigDecimal) fila[6]);
                modeloOrdenes.addRow(fila);
            }
            boolean encontrada = false;
            for (int i = 0; i < modeloOrdenes.getRowCount(); i++) {
                if ((Integer) modeloOrdenes.getValueAt(i, 0) == idSeleccionar) {
                    tblOrdenes.setRowSelectionInterval(i, i);
                    encontrada = true;
                    break;
                }
            }
            if (!encontrada) {
                mostrarDetalle(0);
            }
        });
    }

    private void mostrarDetalle(int idOrden) {
        modeloServicios.setRowCount(0);
        modeloRepuestos.setRowCount(0);
        ordenActual = null;
        if (idOrden > 0) {
            Ui.ejecutar(this, () -> ordenActual = ordenDAO.buscarPorId(idOrden));
        }
        if (ordenActual == null) {
            lblInfo.setText("Seleccione una orden");
            lblTotales.setText(" ");
        } else {
            OrdenTrabajo o = ordenActual;
            lblInfo.setText("<html><b>Orden #" + o.getIdOrden() + "</b> - " + o.getEstado()
                    + "<br>Cliente: " + o.getVehiculo().getCliente().getNombreCompleto()
                    + "<br>Vehículo: " + o.getVehiculo()
                    + "<br>Mecánico: " + o.getMecanico().getNombreCompleto()
                    + "<br>Problema: " + o.getDescripcionProblema() + "</html>");
            for (DetalleServicio d : o.getServicios()) {
                modeloServicios.addRow(new Object[]{d.getDescripcion(), Ui.moneda(d.calcularSubtotal())});
            }
            for (DetalleRepuesto d : o.getRepuestos()) {
                modeloRepuestos.addRow(new Object[]{d.getRepuesto().getNombre(), d.getCantidad(),
                    Ui.moneda(d.getPrecioUnitario()), Ui.moneda(d.calcularSubtotal())});
            }
            lblTotales.setText("<html>Mano de obra: " + Ui.moneda(o.getTotalManoObra())
                    + " &nbsp; Repuestos: " + Ui.moneda(o.getTotalRepuestos())
                    + " &nbsp; <b>Total: " + Ui.moneda(o.getTotal()) + "</b></html>");
        }
        actualizarBotones();
    }

    private void actualizarBotones() {
        EstadoOrden estado = ordenActual == null ? null : ordenActual.getEstado();
        boolean enProceso = estado == EstadoOrden.EN_PROCESO;
        btnIniciar.setEnabled(estado == EstadoOrden.PENDIENTE);
        btnAgregarServicio.setEnabled(enProceso);
        btnQuitarServicio.setEnabled(enProceso);
        btnAgregarRepuesto.setEnabled(enProceso);
        btnQuitarRepuesto.setEnabled(enProceso);
        btnFinalizar.setEnabled(enProceso);
        btnCancelar.setEnabled(estado == EstadoOrden.PENDIENTE || enProceso);
        btnComprobante.setEnabled(estado == EstadoOrden.FINALIZADA);
    }

    private void accionSobreOrden(Ui.Accion accion) {
        if (ordenActual == null) {
            return;
        }
        int id = ordenActual.getIdOrden();
        Ui.ejecutar(this, accion);
        Ui.ejecutar(this, this::cargarRepuestos);
        cargarOrdenes(id);
        mostrarDetalle(id);
    }

    private void agregarServicio() {
        Servicio servicio = (Servicio) cboServicio.getSelectedItem();
        if (servicio == null) {
            Ui.informar(this, "No hay servicios registrados");
            return;
        }
        accionSobreOrden(() -> ordenDAO.agregarServicio(ordenActual.getIdOrden(), servicio));
    }

    private void quitarServicio() {
        int fila = tblServicios.getSelectedRow();
        if (fila < 0) {
            Ui.informar(this, "Seleccione un servicio de la tabla");
            return;
        }
        int idDetalle = ordenActual.getServicios().get(fila).getIdDetalle();
        accionSobreOrden(() -> ordenDAO.quitarServicio(ordenActual.getIdOrden(), idDetalle));
    }

    private void agregarRepuesto() {
        Repuesto repuesto = (Repuesto) cboRepuesto.getSelectedItem();
        if (repuesto == null) {
            Ui.informar(this, "No hay repuestos registrados");
            return;
        }
        int cantidad = (Integer) spnCantidad.getValue();
        accionSobreOrden(() -> ordenDAO.agregarRepuesto(ordenActual.getIdOrden(), repuesto.getIdRepuesto(), cantidad));
    }

    private void quitarRepuesto() {
        int fila = tblRepuestos.getSelectedRow();
        if (fila < 0) {
            Ui.informar(this, "Seleccione un repuesto de la tabla");
            return;
        }
        int idDetalle = ordenActual.getRepuestos().get(fila).getIdDetalle();
        accionSobreOrden(() -> ordenDAO.quitarRepuesto(ordenActual.getIdOrden(), idDetalle));
    }

    private void finalizar() {
        if (ordenActual == null || !Ui.confirmar(this, "¿Finalizar la orden #" + ordenActual.getIdOrden()
                + " con un total de " + Ui.moneda(ordenActual.getTotal()) + "?")) {
            return;
        }
        accionSobreOrden(() -> ordenDAO.finalizar(ordenActual.getIdOrden()));
        if (ordenActual != null && ordenActual.getEstado() == EstadoOrden.FINALIZADA) {
            verComprobante();
        }
    }

    private void cancelar() {
        if (ordenActual == null || !Ui.confirmar(this, "¿Cancelar la orden #" + ordenActual.getIdOrden()
                + "? Los repuestos utilizados regresarán al inventario.")) {
            return;
        }
        accionSobreOrden(() -> ordenDAO.cancelar(ordenActual.getIdOrden()));
    }

    private void verComprobante() {
        if (ordenActual != null) {
            new DlgComprobante(this, ordenActual).setVisible(true);
        }
    }
}
