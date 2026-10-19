package com.taller;

import com.taller.vista.VentanaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el estilo del sistema: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
