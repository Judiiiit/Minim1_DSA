package edu.upc.dsa.models;

public class Vuelo {
    private String id;
    private String modelo;
    private String compañia;

    public Vuelo() {}

    public Vuelo(String id, String name, String compañia) {
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

}