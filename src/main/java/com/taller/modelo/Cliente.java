package com.taller.modelo;

import java.util.ArrayList;

public class Cliente extends Persona {

    private String direccion;
    private String correo;
    private final ArrayList<Vehiculo> vehiculos = new ArrayList<>();

    public Cliente(String id, String nombre, String apellido, String telefono, String direccion, String correo) {
        super(id, nombre, apellido, telefono);
        this.direccion = direccion;
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void agregarVehiculo(Vehiculo vehiculo) {
        vehiculos.add(vehiculo);
    }

    public ArrayList<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    @Override
    public String mostrarInformacion() {
        return "Cliente: " + getNombreCompleto() + " | Tel: " + getTelefono() + " | Vehículos: " + vehiculos.size();
    }
}
