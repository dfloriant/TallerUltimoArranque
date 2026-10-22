package com.taller.util;

import com.taller.dao.ClienteDAO;
import com.taller.dao.RepuestoDAO;
import com.taller.dao.ServicioDAO;
import com.taller.excepciones.ValidacionException;
import com.taller.modelo.Cliente;
import com.taller.modelo.Repuesto;
import com.taller.modelo.Servicio;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;

public class ImportadorCSV {

    public static final String[] COLUMNAS_CLIENTES = {"id_cliente", "nombre", "apellido", "telefono", "direccion", "correo"};
    public static final String[] COLUMNAS_REPUESTOS = {"id_repuesto", "nombre", "descripcion", "precio", "existencia"};
    public static final String[] COLUMNAS_SERVICIOS = {"id_servicio", "descripcion", "costo_mano_obra"};

    private interface ProcesadorFila {
        void procesar(String[] campos) throws SQLException, ValidacionException;
    }

    public static class Resultado {

        private int insertados;
        private final ArrayList<String> errores = new ArrayList<>();

        public int getInsertados() {
            return insertados;
        }

        public int getRechazados() {
            return errores.size();
        }

        public ArrayList<String> getErrores() {
            return errores;
        }
    }

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final RepuestoDAO repuestoDAO = new RepuestoDAO();
    private final ServicioDAO servicioDAO = new ServicioDAO();

    public Resultado importarClientes(File archivo) throws IOException {
        return importar(archivo, COLUMNAS_CLIENTES.length, campos -> {
            String telefono = campos[3];
            if (!telefono.isEmpty() && !telefono.matches("\\d{8}")) {
                throw new ValidacionException("el teléfono debe tener 8 dígitos: " + telefono);
            }
            clienteDAO.insertar(new Cliente(obligatorio(campos[0], "id_cliente"), obligatorio(campos[1], "nombre"),
                    obligatorio(campos[2], "apellido"), telefono, campos[4], campos[5]));
        });
    }

    public Resultado importarRepuestos(File archivo) throws IOException {
        return importar(archivo, COLUMNAS_REPUESTOS.length, campos -> repuestoDAO.insertar(new Repuesto(
                obligatorio(campos[0], "id_repuesto"), obligatorio(campos[1], "nombre"), campos[2],
                decimal(campos[3], "precio"), entero(campos[4], "existencia"))));
    }

    public Resultado importarServicios(File archivo) throws IOException {
        return importar(archivo, COLUMNAS_SERVICIOS.length, campos -> servicioDAO.insertar(new Servicio(
                obligatorio(campos[0], "id_servicio"), obligatorio(campos[1], "descripcion"),
                decimal(campos[2], "costo_mano_obra"))));
    }

    private Resultado importar(File archivo, int columnas, ProcesadorFila procesador) throws IOException {
        Resultado resultado = new Resultado();
        try (BufferedReader lector = Files.newBufferedReader(archivo.toPath(), StandardCharsets.UTF_8)) {
            String linea;
            int numero = 0;
            char separador = ',';
            while ((linea = lector.readLine()) != null) {
                numero++;
                if (numero == 1) {
                    linea = linea.replace("﻿", "");
                }
                if (linea.isBlank()) {
                    continue;
                }
                if (numero == 1 && linea.contains(";") && !linea.contains(",")) {
                    separador = ';';
                }
                String[] campos = separar(linea, separador);
                if (numero == 1 && campos[0].toLowerCase().startsWith("id")) {
                    continue;
                }
                try {
                    if (campos.length != columnas) {
                        throw new ValidacionException("se esperaban " + columnas + " columnas y hay " + campos.length);
                    }
                    procesador.procesar(campos);
                    resultado.insertados++;
                } catch (ValidacionException | SQLException e) {
                    resultado.errores.add("Línea " + numero + ": " + e.getMessage());
                }
            }
        }
        return resultado;
    }

    private static String[] separar(String linea, char separador) {
        ArrayList<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++;
                } else {
                    entreComillas = !entreComillas;
                }
            } else if (c == separador && !entreComillas) {
                campos.add(actual.toString().trim());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString().trim());
        return campos.toArray(new String[0]);
    }

    private static String obligatorio(String valor, String campo) throws ValidacionException {
        if (valor.isEmpty()) {
            throw new ValidacionException("el campo " + campo + " es obligatorio");
        }
        return valor;
    }

    private static BigDecimal decimal(String valor, String campo) throws ValidacionException {
        try {
            BigDecimal numero = new BigDecimal(valor);
            if (numero.signum() < 0) {
                throw new ValidacionException("el campo " + campo + " no puede ser negativo");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new ValidacionException("el campo " + campo + " no es un número válido: " + valor);
        }
    }

    private static int entero(String valor, String campo) throws ValidacionException {
        try {
            int numero = Integer.parseInt(valor);
            if (numero < 0) {
                throw new ValidacionException("el campo " + campo + " no puede ser negativo");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new ValidacionException("el campo " + campo + " no es un entero válido: " + valor);
        }
    }
}
