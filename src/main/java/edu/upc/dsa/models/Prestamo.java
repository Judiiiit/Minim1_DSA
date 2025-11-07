package edu.upc.dsa.models;

public class Prestamo {
    private String id;
    private String lectorId;
    private String libroId;
    private  String inicioPrestamo;
    private  String finalPrestamo;
    private boolean enTramite;

    public Prestamo() {}

    public Prestamo(String id, String lectorId, String libroId, String inicioPrestamo, String finalPrestamo) {
        this.id = id;
        this.lectorId = lectorId;
        this.libroId = libroId;
        this.inicioPrestamo = inicioPrestamo;
        this.finalPrestamo = finalPrestamo;
    }

    // GETTERS & SETTERS
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLectorId() {
        return lectorId;
    }

    public void setLectorId(String lectorId) {
        this.lectorId = lectorId;
    }

    public String getLibroId() {
        return libroId;
    }

    public void setLibroId(String libroId) {
        this.libroId = libroId;
    }

    public String getInicioPrestamo() {
        return inicioPrestamo;
    }

    public void setInicioPrestamo(String inicioPrestamo) {
        this.inicioPrestamo = inicioPrestamo;
    }

    public String getFinalPrestamo() {
        return finalPrestamo;
    }

    public void setFinalPrestamo(String finalPrestamo) {
        this.finalPrestamo = finalPrestamo;
    }

    public boolean isEnTramite() {
        return enTramite;
    }

    public void setEnTramite(boolean enTramite) {
        this.enTramite = enTramite;
    }

    @Override
    public String toString() {
        return "Prestamo{" +
                "id='" + id + '\'' +
                ", lectorId='" + lectorId + '\'' +
                ", libroId='" + libroId + '\'' +
                ", inicioPrestamo='" + inicioPrestamo + '\'' +
                ", finalPrestamo='" + finalPrestamo + '\'' +
                ", enTramite=" + enTramite +
                '}';
    }
}
