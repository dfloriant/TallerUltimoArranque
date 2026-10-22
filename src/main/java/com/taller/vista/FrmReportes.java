package com.taller.vista;

import com.taller.dao.MecanicoDAO;
import com.taller.dao.ReporteDAO;
import com.taller.dao.VehiculoDAO;
import com.taller.excepciones.ValidacionException;
import com.taller.modelo.EstadoOrden;
import com.taller.modelo.Mecanico;
import com.taller.modelo.Vehiculo;
import com.taller.util.ExportadorTabla;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class FrmReportes extends JFrame {

    public static final String[] TITULOS = {
        "Órdenes por estado",
        "Reparaciones por vehículo",
        "Repuestos con bajo inventario",
        "Reparaciones por mecánico",
        "Ingresos por reparaciones"
    };

    private interface Consulta {
        void ejecutar(PanelReporte panel) throws SQLException, ValidacionException;
    }

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final JTabbedPane pestanas = new JTabbedPane();

    public FrmReportes(int pestanaInicial) {
        setTitle("Reportes");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        pestanas.addTab("01 " + TITULOS[0], crearReporteEstado());
        pestanas.addTab("02 " + TITULOS[1], crearReporteVehiculo());
        pestanas.addTab("03 " + TITULOS[2], crearReporteInventario());
        pestanas.addTab("04 " + TITULOS[3], crearReporteMecanico());
        pestanas.addTab("05 " + TITULOS[4], crearReporteIngresos());
        pestanas.setSelectedIndex(pestanaInicial);
        add(pestanas, BorderLayout.CENTER);
    }

    private PanelReporte crearReporteEstado() {
        JComboBox<Object> cboEstado = new JComboBox<>();
        cboEstado.addItem("Todos");
        for (EstadoOrden estado : EstadoOrden.values()) {
            cboEstado.addItem(estado);
        }
        PanelReporte panel = new PanelReporte(TITULOS[0], p -> {
            Object filtro = cboEstado.getSelectedItem();
            EstadoOrden estado = filtro instanceof EstadoOrden ? (EstadoOrden) filtro : null;
            ArrayList<Object[]> filas = reporteDAO.ordenesPorEstado(estado);
            p.mostrar(new String[]{"Orden", "Fecha", "Placa", "Cliente", "Mecánico", "Estado", "Total"}, filas);
            p.resumen(filas.size() + " órdenes | Suma de totales: " + Ui.moneda(sumar(filas, 6)));
        }, new JLabel("Estado:"), cboEstado);
        panel.consultar();
        return panel;
    }

    private PanelReporte crearReporteVehiculo() {
        JComboBox<Vehiculo> cboVehiculo = new JComboBox<>();
        Ui.ejecutar(this, () -> {
            for (Vehiculo v : new VehiculoDAO().listar()) {
                cboVehiculo.addItem(v);
            }
        });
        PanelReporte panel = new PanelReporte(TITULOS[1], p -> {
            Vehiculo vehiculo = (Vehiculo) cboVehiculo.getSelectedItem();
            if (vehiculo == null) {
                throw new ValidacionException("Seleccione un vehículo.");
            }
            ArrayList<Object[]> filas = reporteDAO.reparacionesPorVehiculo(vehiculo.getPlaca());
            p.mostrar(new String[]{"Orden", "Ingreso", "Finalización", "Mecánico", "Estado", "Servicios", "Repuestos", "Total"}, filas);
            p.resumen(filas.size() + " reparaciones del vehículo " + vehiculo + " | Suma de totales: " + Ui.moneda(sumar(filas, 7)));
        }, new JLabel("Vehículo:"), cboVehiculo);
        return panel;
    }

    private PanelReporte crearReporteInventario() {
        PanelReporte panel = new PanelReporte(TITULOS[2], p -> {
            ArrayList<Object[]> filas = reporteDAO.repuestosBajoInventario();
            p.mostrar(new String[]{"Código", "Repuesto", "Descripción", "Precio", "Existencia", "Estado"}, filas);
            p.resumen(filas.size() + " repuestos con existencia menor o igual a 5");
        }, new JLabel("Repuestos con existencia menor o igual a 5"));
        panel.consultar();
        return panel;
    }

    private PanelReporte crearReporteMecanico() {
        JComboBox<Object> cboMecanico = new JComboBox<>();
        cboMecanico.addItem("Todos (resumen)");
        Ui.ejecutar(this, () -> {
            for (Mecanico m : new MecanicoDAO().listar()) {
                cboMecanico.addItem(m);
            }
        });
        PanelReporte panel = new PanelReporte(TITULOS[3], p -> {
            Object seleccion = cboMecanico.getSelectedItem();
            if (seleccion instanceof Mecanico) {
                Mecanico m = (Mecanico) seleccion;
                ArrayList<Object[]> filas = reporteDAO.reparacionesPorMecanico(m.getId());
                p.mostrar(new String[]{"Mecánico", "Orden", "Fecha", "Placa", "Estado", "Total"}, filas);
                p.resumen(filas.size() + " órdenes asignadas a " + m.getNombreCompleto());
            } else {
                ArrayList<Object[]> filas = reporteDAO.resumenPorMecanico();
                p.mostrar(new String[]{"Mecánico", "Total órdenes", "Pendientes", "En proceso", "Finalizadas", "Canceladas"}, filas);
                p.resumen(filas.size() + " mecánicos");
            }
        }, new JLabel("Mecánico:"), cboMecanico);
        panel.consultar();
        return panel;
    }

    private PanelReporte crearReporteIngresos() {
        JSpinner spnDesde = crearSpinnerFecha(LocalDate.now().withDayOfMonth(1));
        JSpinner spnHasta = crearSpinnerFecha(LocalDate.now());
        PanelReporte panel = new PanelReporte(TITULOS[4], p -> {
            LocalDate desde = fecha(spnDesde);
            LocalDate hasta = fecha(spnHasta);
            if (desde.isAfter(hasta)) {
                throw new ValidacionException("La fecha inicial no puede ser mayor que la fecha final.");
            }
            ArrayList<Object[]> filas = reporteDAO.ingresos(desde, hasta);
            p.mostrar(new String[]{"Orden", "Finalización", "Placa", "Cliente", "Mecánico", "Mano de obra", "Repuestos", "Total"}, filas);
            if (!filas.isEmpty()) {
                p.agregarFila(new Object[]{"TOTAL", "", "", "", "", dinero(sumar(filas, 5)), dinero(sumar(filas, 6)), dinero(sumar(filas, 7))});
            }
            p.resumen(filas.size() + " órdenes finalizadas del " + desde + " al " + hasta
                    + " | Ingresos: " + Ui.moneda(sumar(filas, 7)));
        }, new JLabel("Desde:"), spnDesde, new JLabel("Hasta:"), spnHasta);
        panel.consultar();
        return panel;
    }

    private JSpinner crearSpinnerFecha(LocalDate inicial) {
        Date valor = Date.from(inicial.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner spinner = new JSpinner(new SpinnerDateModel(valor, null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        return spinner;
    }

    private LocalDate fecha(JSpinner spinner) {
        return ((Date) spinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static BigDecimal sumar(ArrayList<Object[]> filas, int columna) {
        BigDecimal total = BigDecimal.ZERO;
        for (Object[] fila : filas) {
            if (fila[columna] instanceof BigDecimal) {
                total = total.add((BigDecimal) fila[columna]);
            }
        }
        return total;
    }

    private static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private class PanelReporte extends JPanel {

        private final String titulo;
        private final Consulta consulta;
        private final DefaultTableModel modelo = Ui.modeloTabla();
        private final JTable tabla = new JTable(modelo);
        private final JLabel lblResumen = new JLabel(" ");

        PanelReporte(String titulo, Consulta consulta, JComponent... filtros) {
            super(new BorderLayout(5, 5));
            this.titulo = titulo;
            this.consulta = consulta;
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT));
            for (JComponent filtro : filtros) {
                norte.add(filtro);
            }
            JButton btnConsultar = new JButton("Consultar");
            JButton btnHtml = new JButton("Exportar HTML");
            JButton btnExcel = new JButton("Exportar Excel (CSV)");
            norte.add(btnConsultar);
            norte.add(btnHtml);
            norte.add(btnExcel);

            tabla.setAutoCreateRowSorter(true);
            add(norte, BorderLayout.NORTH);
            add(new JScrollPane(tabla), BorderLayout.CENTER);
            add(lblResumen, BorderLayout.SOUTH);

            btnConsultar.addActionListener(e -> consultar());
            btnHtml.addActionListener(e -> exportar(true));
            btnExcel.addActionListener(e -> exportar(false));
        }

        void consultar() {
            Ui.ejecutar(FrmReportes.this, () -> consulta.ejecutar(this));
        }

        void mostrar(String[] columnas, ArrayList<Object[]> filas) {
            modelo.setColumnIdentifiers(columnas);
            modelo.setRowCount(0);
            for (Object[] fila : filas) {
                for (int i = 0; i < fila.length; i++) {
                    if (fila[i] instanceof BigDecimal) {
                        fila[i] = dinero((BigDecimal) fila[i]);
                    }
                }
                modelo.addRow(fila);
            }
        }

        void agregarFila(Object[] fila) {
            modelo.addRow(fila);
        }

        void resumen(String texto) {
            lblResumen.setText(texto);
        }

        private void exportar(boolean html) {
            if (modelo.getRowCount() == 0) {
                Ui.informar(FrmReportes.this, "No hay datos para exportar. Presione Consultar primero.");
                return;
            }
            String extension = html ? "html" : "csv";
            JFileChooser selector = new JFileChooser();
            selector.setFileFilter(new FileNameExtensionFilter(html ? "Página HTML" : "Archivo CSV (Excel)", extension));
            selector.setSelectedFile(new File(titulo.toLowerCase().replace(" ", "_") + "." + extension));
            if (selector.showSaveDialog(FrmReportes.this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File archivo = selector.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith("." + extension)) {
                archivo = new File(archivo.getParentFile(), archivo.getName() + "." + extension);
            }
            try {
                if (html) {
                    ExportadorTabla.aHtml(tabla, titulo, archivo);
                } else {
                    ExportadorTabla.aCsv(tabla, archivo);
                }
                Ui.informar(FrmReportes.this, "Reporte exportado en " + archivo.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(FrmReportes.this, "No se pudo exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
