package edu.upc.dsa;

import edu.upc.dsa.models.Vuelo;
import edu.upc.dsa.models.Maleta;
import edu.upc.dsa.models.Avion;
import edu.upc.dsa.exceptions.*;

import java.util.*;

import org.apache.log4j.Logger;

public class VuelosManagerImpl implements VuelosManager {
    private HashMap<String, Avion> aviones;
    private HashMap<String, Vuelo> vuelos;

    final static Logger logger = Logger.getLogger(VuelosManagerImpl.class);

    private static VuelosManager instance;

    private VuelosManagerImpl() {
        this.aviones = new HashMap<>();
        this.vuelos = new HashMap<>();
    }

    public static VuelosManager getInstance() {
        if (instance==null) instance = new VuelosManagerImpl();
        return instance;
    }

    @Override
    public void addAvion(String id, String modelo, String compañia) {
        logger.info("Añadiendo avión: id = " + id + ", modelo = " + modelo + ", compañia = " + compañia);
        try {
            if (aviones.containsKey(id)) {
                Avion avion = aviones.get(id);
                if (avion.getModelo().equals(modelo) && avion.getCompañia().equals(compañia)) {
                    logger.error("No se pueden usar los mismos valores de modelo y compañía para un ID existente");
                    throw new MismosParametrosPorIdException("El avión con ID " + id + " ya tiene esos valores.");
                }
                else {
                    avion.setModelo(modelo);
                    avion.setCompañia(compañia);
                    logger.info("Avión actualizado: " + avion);
                }
            }
            else {
                Avion avion = new Avion(id, modelo, compañia);
                aviones.put(id, avion);
                logger.info("Avión añadido: " + avion);
            }
        }
        catch (MismosParametrosPorIdException ex) {
            logger.error("Excepción mismos parametros con mismo id: ", ex);
        }
    }
    @Override
    public void addVuelo(String id, Date horaSalida, Date horaLlegada, String avionId, String origen, String destino) {
        logger.info("Añadiendo Vuelo: id =" + id + ", hora salida =" + horaSalida + ", hora llegada =" + horaLlegada + ", aviónId =" + avionId + ", origen =" + origen + ", destino=" + destino);
        try {
            if (!aviones.containsKey(avionId)) {
                logger.error("No se puede crear el vuelo porque el avión con id " + avionId + " no existe");
                throw new AvionNotFoundException("El avión con id " + avionId + " no existe");
            }
            if (horaSalida == null || horaLlegada == null || !horaLlegada.after(horaSalida)) {
                logger.error("Fechas inválidas: horaLlegada debe ser posterior a horaSalida");
                throw new BadRequestException("Las fechas del vuelo no son válidas");
            }
            // Si el vuelo ya existe
            if (vuelos.containsKey(id)) {
                Vuelo vuelo = vuelos.get(id);
                // Si tiene el mismo avión, origen y destino, no tiene sentido actualizar
                if (vuelo.getAvionId().equals(avionId) && vuelo.getOrigen().equals(origen) && vuelo.getDestino().equals(destino)) {
                    logger.error("No se pueden usar los mismos valores para un vuelo con el mismo ID");
                    throw new MismosParametrosPorIdException("El vuelo con ID " + id + " ya tiene esos valores (avión, origen, destino)");
                }
                else {
                    vuelo.setHoraSalida(horaSalida);
                    vuelo.setHoraLlegada(horaLlegada);
                    vuelo.setAvionId(avionId);
                    vuelo.setOrigen(origen);
                    vuelo.setDestino(destino);
                    logger.info("Vuelo actualizado: " + vuelo);
                }
            }
            else {
                Vuelo vuelo = new Vuelo(id, horaSalida, horaLlegada, avionId, origen, destino);
                vuelos.put(id, vuelo);
                logger.info("Vuelo añadido correctamente: " + vuelo);
            }
        }
        catch (AvionNotFoundException | BadRequestException | MismosParametrosPorIdException ex) {
            logger.error("Excepción al añadir vuelo: ", ex);
        }
    }
    @Override
    public void facturarMaletaUsuario (String flightId, Maleta maleta) {
        logger.info("Facturando maleta: idMaleta = " + maleta.getId() + ", vuelo = " + flightId);
        try {
            if (!vuelos.containsKey(flightId)) {
                logger.error("Error: el vuelo con id " + flightId + " no existe");
                throw new VueloNotFoundException("El vuelo con id " + flightId + " no existe");
            }
            Vuelo vuelo = vuelos.get(flightId);
            maleta.setFlightId(flightId);
            vuelo.addMaleta(maleta);
            logger.info("Maleta facturada correctamente: " + maleta);
        }
        catch (VueloNotFoundException ex) {
            logger.error("Excepción facturar maleta: vuelo no encontrado", ex);
        }
    }
    @Override
    public List<Maleta> getMaletasFacturadas(String flightId) {
        logger.info("Obteniendo maletas facturadas del vuelo " + flightId);
        List<Maleta> listaMaletas = new ArrayList<>();
        try {
            if (!vuelos.containsKey(flightId)) {
                logger.error("Error: el vuelo con id " + flightId + " no existe");
                throw new VueloNotFoundException("El vuelo con id " + flightId + " no existe");
            }
            Vuelo vuelo = vuelos.get(flightId);
            Stack<Maleta> stackMaletas = vuelo.getMaletas();
            listaMaletas = new ArrayList<>(stackMaletas);
            logger.info("Se han recuperado " + listaMaletas.size() + " maletas");
        }
        catch (VueloNotFoundException ex) {
            logger.error("Excepción al obtener maletas facturadas: vuelo no encontrado", ex);
        }
        return listaMaletas;
    }
    @Override
    public void clear() {
        logger.info("clear(): start");
        aviones.clear();
        vuelos.clear();
        logger.info("clear(): end");
    }

    @Override
    public int avionesSize() {
        int ret = this.aviones.size();
        logger.info("size " + ret);
        return ret;
    }
    @Override
    public Avion getAvion(String id) {
        logger.info("Buscando avión con id = " + id);
        try {
            if (!aviones.containsKey(id)) {
                logger.error("El avión con id " + id + " no existe");
                throw new AvionNotFoundException("El avión con id " + id + " no existe");
            }
            return aviones.get(id);
        }
        catch (AvionNotFoundException ex) {
            logger.error("Excepción buscar avión: ", ex);
            return null;
        }
    }
    @Override
    public Vuelo getVuelo(String id) {
        logger.info("Buscando vuelo con id = " + id);
        try {
            if (!vuelos.containsKey(id)) {
                logger.error("El vuelo con id " + id + " no existe");
                throw new VueloNotFoundException("El vuelo con id " + id + " no existe");
            }
            return vuelos.get(id);
        }
        catch (VueloNotFoundException ex) {
            logger.error("Excepción buscar avión: ", ex);
            return null;
        }
    }
    @Override
    public List<Avion> getAllAviones() {
        logger.info("Obteniendo todos los aviones");
        return new ArrayList<>(aviones.values());
    }
    @Override
    public List<Vuelo> getAllVuelos() {
        logger.info("Obteniendo todos los vuelos");
        return new ArrayList<>(vuelos.values());
    }
    @Override
    public Avion updateAvion(Avion avion) {
        Avion a = this.getAvion(avion.getId());
        logger.info("Actualizando vuelo con id = " + avion.getId());

        if (a!=null) {
            a.setModelo(avion.getModelo());
            a.setCompañia(avion.getCompañia());
            logger.info("Avión actualizado: " + avion);
        }
        else {
            logger.warn("Avion no encontrado "+ avion);
        }
        return a;
    }
    public Vuelo updateVuelo(Vuelo vuelo) {
        Vuelo v = this.getVuelo(vuelo.getId());
        logger.info("Actualizando vuelo con id = " + vuelo.getId());

        if (v!=null) {
            v.setHoraSalida(vuelo.getHoraSalida());
            v.setHoraLlegada(vuelo.getHoraLlegada());
            v.setAvionId(vuelo.getAvionId());
            v.setOrigen(vuelo.getOrigen());
            v.setDestino(vuelo.getDestino());
            logger.info("Vuelo actualizado: " + vuelo);
        }
        else {
            logger.warn("Vuelo no encontrado "+ vuelo);
        }
        return v;
    }
}