package edu.upc.dsa.models;

import java.util.Date;
import java.util.Stack;


public class Vuelo {
    private String id;
    private  Date horaSalida;
    private  Date horaLlegada;
    private String avionId;
    private String origen;
    private String destino;
    private Stack<Maleta> maletas;

    public Vuelo () {}

    public Vuelo(String id, Date horaSalida, Date horaLlegada, String avionId, String origen, String destino) {
        this.id = id;
        this.horaSalida = horaSalida;
        this.horaLlegada = horaLlegada;
        this.avionId = avionId;
        this.origen = origen;
        this.destino = destino;
        this.maletas = new Stack<Maleta>();
    }

    // GETTERS & SETTERS

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(Date horaSalida) {
        this.horaSalida = horaSalida;
    }

    public Date getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(Date horaLlegada) {
        this.horaLlegada = horaLlegada;
    }

    public String getAvionId() {
        return avionId;
    }

    public void setAvionId(String avionId) {
        this.avionId = avionId;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public Stack<Maleta> getMaletas() {
        return maletas;
    }

    public void addMaleta(Maleta maleta) {
        this.maletas.push(maleta);
    }

    @Override
    public String toString() {
        return "Vuelo{" +
                "id='" + id + '\'' +
                ", horaSalida=" + horaSalida +
                ", horaLlegada=" + horaLlegada +
                ", avionId='" + avionId + '\'' +
                ", origen='" + origen + '\'' +
                ", destino='" + destino + '\'' +
                ", maletas=" + maletas +
                '}';
    }
}
