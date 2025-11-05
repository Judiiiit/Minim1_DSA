package edu.upc.dsa;

import edu.upc.dsa.models.Maleta;
import edu.upc.dsa.models.Avion;
import edu.upc.dsa.models.Vuelo;

import java.util.Date;
import java.util.List;

public interface VuelosManager {
    // FUNCIONS DEMANADES
    public void addAvion(String id, String modelo, String compañia);
    public void addVuelo(String id, Date horaSalida, Date horaLlegada, String avionId, String origen, String destino);
    public void facturarMaletaUsuario (String flightId, Maleta maleta);
    public List<Maleta> getMaletasFacturadas(String flightId);

    // FUNCIONS PER LA RESAPI
    public int avionesSize();
    public Avion getAvion(String id);
    public Vuelo getVuelo(String id);
    public List<Avion> getAllAviones();
    public List<Vuelo> getAllVuelos();
    public Avion updateAvion(Avion avion);
    public Vuelo updateVuelo(Vuelo vuelo);
    public void clear();
}
