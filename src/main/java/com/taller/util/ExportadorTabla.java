package com.taller.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public final class ExportadorTabla {

    private ExportadorTabla() {
    }

    public static void aHtml(JTable tabla, String titulo, File archivo) throws IOException {
        TableModel modelo = tabla.getModel();
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        try (BufferedWriter w = Files.newBufferedWriter(archivo.toPath(), StandardCharsets.UTF_8)) {
            w.write("<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n<meta charset=\"UTF-8\">\n");
            w.write("<title>" + escaparHtml(titulo) + "</title>\n");
            w.write("<style>body{font-family:Arial,sans-serif;margin:24px}"
                    + "table{border-collapse:collapse;width:100%}"
                    + "th,td{border:1px solid #999;padding:6px 8px;text-align:left}"
                    + "th{background:#1f4e79;color:#fff}tr:nth-child(even){background:#f2f2f2}</style>\n");
            w.write("</head>\n<body>\n");
            w.write("<h2>Taller El Último Arranque</h2>\n");
            w.write("<h3>" + escaparHtml(titulo) + "</h3>\n");
            w.write("<p>Generado: " + fecha + " | Registros: " + modelo.getRowCount() + "</p>\n");
            w.write("<table>\n<tr>");
            for (int c = 0; c < modelo.getColumnCount(); c++) {
                w.write("<th>" + escaparHtml(modelo.getColumnName(c)) + "</th>");
            }
            w.write("</tr>\n");
            for (int f = 0; f < modelo.getRowCount(); f++) {
                w.write("<tr>");
                for (int c = 0; c < modelo.getColumnCount(); c++) {
                    w.write("<td>" + escaparHtml(valor(modelo.getValueAt(f, c))) + "</td>");
                }
                w.write("</tr>\n");
            }
            w.write("</table>\n</body>\n</html>\n");
        }
    }

    public static void aCsv(JTable tabla, File archivo) throws IOException {
        TableModel modelo = tabla.getModel();
        String[] encabezados = new String[modelo.getColumnCount()];
        for (int c = 0; c < encabezados.length; c++) {
            encabezados[c] = modelo.getColumnName(c);
        }
        try (BufferedWriter w = abrirCsv(archivo)) {
            w.write(lineaCsv(encabezados));
            for (int f = 0; f < modelo.getRowCount(); f++) {
                Object[] fila = new Object[encabezados.length];
                for (int c = 0; c < fila.length; c++) {
                    fila[c] = modelo.getValueAt(f, c);
                }
                w.write(lineaCsv(fila));
            }
        }
    }

    public static void aCsv(String[] encabezados, List<Object[]> filas, File archivo) throws IOException {
        try (BufferedWriter w = abrirCsv(archivo)) {
            w.write(lineaCsv(encabezados));
            for (Object[] fila : filas) {
                w.write(lineaCsv(fila));
            }
        }
    }

    private static BufferedWriter abrirCsv(File archivo) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(archivo.toPath(), StandardCharsets.UTF_8);
        w.write('﻿');
        return w;
    }

    private static String lineaCsv(Object[] valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            String v = valor(valores[i]);
            if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
                v = "\"" + v.replace("\"", "\"\"") + "\"";
            }
            sb.append(v);
        }
        return sb.append("\r\n").toString();
    }

    private static String valor(Object o) {
        return o == null ? "" : o.toString();
    }

    private static String escaparHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
