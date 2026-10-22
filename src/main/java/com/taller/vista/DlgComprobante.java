package com.taller.vista;

import com.taller.modelo.DetalleRepuesto;
import com.taller.modelo.DetalleServicio;
import com.taller.modelo.OrdenTrabajo;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DlgComprobante extends JDialog {

    private final OrdenTrabajo orden;
    private final String html;

    public DlgComprobante(Window padre, OrdenTrabajo orden) {
        super(padre, "Comprobante de la orden #" + orden.getIdOrden(), ModalityType.APPLICATION_MODAL);
        this.orden = orden;
        this.html = generarHtml();
        setSize(650, 700);
        setLocationRelativeTo(padre);

        JEditorPane editor = new JEditorPane("text/html", html);
        editor.setEditable(false);
        editor.setCaretPosition(0);

        JButton btnGuardar = new JButton("Guardar HTML");
        JButton btnImprimir = new JButton("Imprimir");
        JButton btnCerrar = new JButton("Cerrar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnGuardar);
        botones.add(btnImprimir);
        botones.add(btnCerrar);

        add(new JScrollPane(editor), BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        btnGuardar.addActionListener(e -> guardar());
        btnImprimir.addActionListener(e -> {
            try {
                editor.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo imprimir: " + ex.getMessage());
            }
        });
        btnCerrar.addActionListener(e -> dispose());
    }

    private String generarHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset='UTF-8'><title>Comprobante orden ").append(orden.getIdOrden())
                .append("</title></head><body style='font-family:sans-serif'>");
        sb.append("<h2 style='text-align:center'>Taller El Último Arranque</h2>");
        sb.append("<h3 style='text-align:center'>Comprobante de orden de trabajo #").append(orden.getIdOrden()).append("</h3>");
        sb.append("<p><b>Fecha de ingreso:</b> ").append(fecha(orden.getFechaCreacion()))
                .append("<br><b>Fecha de finalización:</b> ").append(fecha(orden.getFechaFinalizacion()))
                .append("<br><b>Estado:</b> ").append(orden.getEstado()).append("</p>");
        sb.append("<p><b>Cliente:</b> ").append(texto(orden.getVehiculo().getCliente().getNombreCompleto()))
                .append("<br><b>Teléfono:</b> ").append(texto(orden.getVehiculo().getCliente().getTelefono()))
                .append("<br><b>Vehículo:</b> ").append(texto(orden.getVehiculo().getPlaca())).append(" - ")
                .append(texto(orden.getVehiculo().getMarca())).append(" ").append(texto(orden.getVehiculo().getModelo()))
                .append("<br><b>Mecánico:</b> ").append(texto(orden.getMecanico().getNombreCompleto()))
                .append("<br><b>Problema reportado:</b> ").append(texto(orden.getDescripcionProblema())).append("</p>");

        sb.append("<h4>Servicios realizados</h4><table border='1' cellspacing='0' cellpadding='4' width='100%'>")
                .append("<tr><th>Servicio</th><th>Costo</th></tr>");
        for (DetalleServicio d : orden.getServicios()) {
            sb.append("<tr><td>").append(texto(d.getDescripcion())).append("</td><td align='right'>")
                    .append(Ui.moneda(d.calcularSubtotal())).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<h4>Repuestos utilizados</h4><table border='1' cellspacing='0' cellpadding='4' width='100%'>")
                .append("<tr><th>Repuesto</th><th>Cantidad</th><th>Precio unitario</th><th>Subtotal</th></tr>");
        for (DetalleRepuesto d : orden.getRepuestos()) {
            sb.append("<tr><td>").append(texto(d.getRepuesto().getNombre())).append("</td><td align='center'>")
                    .append(d.getCantidad()).append("</td><td align='right'>").append(Ui.moneda(d.getPrecioUnitario()))
                    .append("</td><td align='right'>").append(Ui.moneda(d.calcularSubtotal())).append("</td></tr>");
        }
        if (orden.getRepuestos().isEmpty()) {
            sb.append("<tr><td colspan='4' align='center'>Sin repuestos</td></tr>");
        }
        sb.append("</table>");

        sb.append("<p align='right'><b>Total mano de obra:</b> ").append(Ui.moneda(orden.getTotalManoObra()))
                .append("<br><b>Total repuestos:</b> ").append(Ui.moneda(orden.getTotalRepuestos()))
                .append("<br><b style='font-size:14pt'>TOTAL A PAGAR: ").append(Ui.moneda(orden.getTotal())).append("</b></p>");
        sb.append("<p style='text-align:center'>Gracias por su preferencia</p></body></html>");
        return sb.toString();
    }

    private String fecha(LocalDate f) {
        return f == null ? "-" : f.toString();
    }

    private String texto(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void guardar() {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Página HTML", "html"));
        selector.setSelectedFile(new File("comprobante_orden_" + orden.getIdOrden() + ".html"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".html")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".html");
        }
        try {
            Files.writeString(archivo.toPath(), html, StandardCharsets.UTF_8);
            Ui.informar(this, "Comprobante guardado en " + archivo.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar: " + ex.getMessage());
        }
    }
}
