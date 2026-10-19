package com.taller;

import com.taller.dao.Conexion;
import java.sql.Connection;
import java.sql.SQLException;

public class PruebaConexion {

    public static void main(String[] args) {
        try (Connection cn = Conexion.getConexion()) {
            System.out.println("Conexión exitosa a " + cn.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }
}
