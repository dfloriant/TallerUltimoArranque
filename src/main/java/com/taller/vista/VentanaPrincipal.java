package com.taller.vista;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal() {
        setTitle("Taller El Último Arranque");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(crearMenu());
        JLabel titulo = new JLabel("Taller El Último Arranque", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        add(titulo, BorderLayout.CENTER);
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu catalogos = new JMenu("Catálogos");
        catalogos.add(item("Clientes"));
        catalogos.add(item("Vehículos"));
        catalogos.add(item("Mecánicos"));
        catalogos.add(item("Repuestos"));
        catalogos.add(item("Servicios"));

        JMenu ordenes = new JMenu("Órdenes");
        ordenes.add(item("Recepción de vehículo"));
        ordenes.add(item("Gestión de órdenes"));

        JMenu reportes = new JMenu("Reportes");
        reportes.add(item("Órdenes por estado"));
        reportes.add(item("Reparaciones por vehículo"));
        reportes.add(item("Repuestos con bajo inventario"));
        reportes.add(item("Reparaciones por mecánico"));
        reportes.add(item("Ingresos por reparaciones"));

        JMenu herramientas = new JMenu("Herramientas");
        herramientas.add(item("Importar CSV"));
        herramientas.add(item("Exportar CSV"));

        barra.add(catalogos);
        barra.add(ordenes);
        barra.add(reportes);
        barra.add(herramientas);
        return barra;
    }

    private JMenuItem item(String texto) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(e -> JOptionPane.showMessageDialog(this, texto + ": pendiente de implementar"));
        return item;
    }
}
