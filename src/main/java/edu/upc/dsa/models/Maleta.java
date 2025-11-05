package edu.upc.dsa.models;

public class Maleta {
    private String id;
    private String flightId;

    public Maleta() {}

    public Maleta(String id) {
        this.id = id;
        this.flightId = flightId;
    }

    // GETTERS AND SETTERS
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    @Override
    public String toString() {
        return "Maleta{" +
                "id='" + id + '\'' +
                ", flightId='" + flightId + '\'' +
                '}';
    }
}
