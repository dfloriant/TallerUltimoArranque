package com.taller.vista;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
        catalogos.add(item("Clientes", () -> new FrmClientes().setVisible(true)));
        catalogos.add(item("Vehículos", () -> new FrmVehiculos().setVisible(true)));
        catalogos.add(item("Mecánicos", () -> new FrmMecanicos().setVisible(true)));
        catalogos.add(item("Repuestos", () -> new FrmRepuestos().setVisible(true)));
        catalogos.add(item("Servicios", () -> new FrmServicios().setVisible(true)));

        JMenu ordenes = new JMenu("Órdenes");
        ordenes.add(item("Recepción de vehículo", () -> new FrmRecepcion().setVisible(true)));
        ordenes.add(item("Gestión de órdenes", () -> new FrmOrdenes().setVisible(true)));

        JMenu reportes = new JMenu("Reportes");
        for (int i = 0; i < FrmReportes.TITULOS.length; i++) {
            int pestana = i;
            reportes.add(item(FrmReportes.TITULOS[i], () -> new FrmReportes(pestana).setVisible(true)));
        }

        JMenu importar = new JMenu("Importar CSV");
        JMenu exportar = new JMenu("Exportar CSV");
        for (HerramientasCSV.Catalogo catalogo : HerramientasCSV.Catalogo.values()) {
            importar.add(item(catalogo.toString(), () -> HerramientasCSV.importar(this, catalogo)));
            exportar.add(item(catalogo.toString(), () -> HerramientasCSV.exportar(this, catalogo)));
        }
        JMenu herramientas = new JMenu("Herramientas");
        herramientas.add(importar);
        herramientas.add(exportar);

        barra.add(catalogos);
        barra.add(ordenes);
        barra.add(reportes);
        barra.add(herramientas);
        return barra;
    }

    private JMenuItem item(String texto, Runnable accion) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(e -> accion.run());
        return item;
    }
}
