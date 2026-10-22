package com.taller.vista;

import com.taller.dao.ClienteDAO;
import com.taller.dao.RepuestoDAO;
import com.taller.dao.ServicioDAO;
import com.taller.modelo.Cliente;
import com.taller.modelo.Repuesto;
import com.taller.modelo.Servicio;
import com.taller.util.ExportadorTabla;
import com.taller.util.ImportadorCSV;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class HerramientasCSV {

    public enum Catalogo {
        CLIENTES("clientes", ImportadorCSV.COLUMNAS_CLIENTES),
        REPUESTOS("repuestos", ImportadorCSV.COLUMNAS_REPUESTOS),
        SERVICIOS("servicios", ImportadorCSV.COLUMNAS_SERVICIOS);

        private final String nombre;
        private final String[] columnas;

        Catalogo(String nombre, String[] columnas) {
            this.nombre = nombre;
            this.columnas = columnas;
        }

        @Override
        public String toString() {
            return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);
        }
    }

    private HerramientasCSV() {
    }

    public static void exportar(Component padre, Catalogo catalogo) {
        File archivo = elegirArchivo(padre, catalogo.nombre + ".csv", true);
        if (archivo == null) {
            return;
        }
        Ui.ejecutar(padre, () -> {
            ArrayList<Object[]> filas = filas(catalogo);
            try {
                ExportadorTabla.aCsv(catalogo.columnas, filas, archivo);
                Ui.informar(padre, filas.size() + " registros de " + catalogo.nombre + " exportados en " + archivo.getAbsolutePath());
            } catch (IOException e) {
                error(padre, "No se pudo escribir el archivo: " + e.getMessage());
            }
        });
    }

    public static void importar(Component padre, Catalogo catalogo) {
        String formato = String.join(",", catalogo.columnas);
        Ui.informar(padre, "El archivo debe tener las columnas en este orden:\n" + formato
                + "\nLa primera línea puede ser el encabezado.");
        File archivo = elegirArchivo(padre, null, false);
        if (archivo == null) {
            return;
        }
        ImportadorCSV importador = new ImportadorCSV();
        try {
            ImportadorCSV.Resultado r;
            switch (catalogo) {
                case CLIENTES:
                    r = importador.importarClientes(archivo);
                    break;
                case REPUESTOS:
                    r = importador.importarRepuestos(archivo);
                    break;
                default:
                    r = importador.importarServicios(archivo);
                    break;
            }
            mostrarResultado(padre, catalogo, r);
        } catch (IOException e) {
            error(padre, "No se pudo leer el archivo: " + e.getMessage());
        }
    }

    private static ArrayList<Object[]> filas(Catalogo catalogo) throws SQLException {
        ArrayList<Object[]> filas = new ArrayList<>();
        switch (catalogo) {
            case CLIENTES:
                for (Cliente c : new ClienteDAO().listar()) {
                    filas.add(new Object[]{c.getId(), c.getNombre(), c.getApellido(), c.getTelefono(), c.getDireccion(), c.getCorreo()});
                }
                break;
            case REPUESTOS:
                for (Repuesto r : new RepuestoDAO().listar()) {
                    filas.add(new Object[]{r.getIdRepuesto(), r.getNombre(), r.getDescripcion(), r.getPrecio(), r.getExistencia()});
                }
                break;
            default:
                for (Servicio s : new ServicioDAO().listar()) {
                    filas.add(new Object[]{s.getIdServicio(), s.getDescripcion(), s.getCostoManoObra()});
                }
                break;
        }
        return filas;
    }

    private static void mostrarResultado(Component padre, Catalogo catalogo, ImportadorCSV.Resultado r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Importación de ").append(catalogo.nombre).append("\n\n");
        sb.append("Registros insertados: ").append(r.getInsertados()).append("\n");
        sb.append("Registros rechazados: ").append(r.getRechazados()).append("\n");
        if (!r.getErrores().isEmpty()) {
            sb.append("\nDetalle de rechazos:\n");
            for (String e : r.getErrores()) {
                sb.append(" - ").append(e).append("\n");
            }
        }
        JTextArea area = new JTextArea(sb.toString(), 15, 60);
        area.setEditable(false);
        JOptionPane.showMessageDialog(padre, new JScrollPane(area), "Resultado de la importación",
                r.getRechazados() == 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private static File elegirArchivo(Component padre, String sugerido, boolean guardar) {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Archivo CSV", "csv"));
        if (sugerido != null) {
            selector.setSelectedFile(new File(sugerido));
        }
        int opcion = guardar ? selector.showSaveDialog(padre) : selector.showOpenDialog(padre);
        if (opcion != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File archivo = selector.getSelectedFile();
        if (guardar && !archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
        }
        return archivo;
    }

    private static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
