package com.taller.modelo;

public enum EstadoOrden {
    PENDIENTE("PENDIENTE"),
    EN_PROCESO("EN PROCESO"),
    FINALIZADA("FINALIZADA"),
    CANCELADA("CANCELADA");

    private final String valorBD;

    EstadoOrden(String valorBD) {
        this.valorBD = valorBD;
    }

    public String getValorBD() {
        return valorBD;
    }

    public static EstadoOrden desdeBD(String valor) {
        for (EstadoOrden e : values()) {
            if (e.valorBD.equals(valor)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + valor);
    }

    @Override
    public String toString() {
        return valorBD;
    }
}
