package edu.upc.dsa.models;

public class Avion {
    private String id;
    private String modelo;
    private String compañia;

    public Avion() {}

    public Avion(String id, String modelo, String compañia) {
        this.id = id;
        this.modelo = modelo;
        this.compañia = compañia;
    }

    // GETTERS AND SETTERS
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id=id;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getCompañia() {
        return compañia;
    }

    public void setCompañia(String compañia) {
        this.compañia = compañia;
    }

    @Override
    public String toString() {
        return "Avion{" +
                "id='" + id + '\'' +
                ", modelo='" + modelo + '\'' +
                ", compañia='" + compañia + '\'' +
                '}';
    }
}