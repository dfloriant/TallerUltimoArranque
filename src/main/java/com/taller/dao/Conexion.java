package com.taller.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL = "jdbc:oracle:thin:@localhost:1521/XEPDB1";
    private static final String USER = "taller";
    private static final String PASS = "taller123";

    private Conexion() {
    }

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
