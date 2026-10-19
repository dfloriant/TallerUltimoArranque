package com.taller.modelo;

public class Mecanico extends Persona {

    private String especialidad;

    public Mecanico(String id, String nombre, String apellido, String telefono, String especialidad) {
        super(id, nombre, apellido, telefono);
        this.especialidad = especialidad;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    @Override
    public String mostrarInformacion() {
        return "Mecánico: " + getNombreCompleto() + " | Especialidad: " + especialidad;
    }
}
