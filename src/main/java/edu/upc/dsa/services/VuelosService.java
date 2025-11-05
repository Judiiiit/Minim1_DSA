package edu.upc.dsa.services;

import edu.upc.dsa.VuelosManager;
import edu.upc.dsa.VuelosManagerImpl;
import edu.upc.dsa.models.Avion;
import edu.upc.dsa.models.Vuelo;
import edu.upc.dsa.models.Maleta;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Date;

@Api(value = "/vuelosManager", description = "Endpoint to manage flights, planes, and luggage")
@Path("/vuelosManager")

public class VuelosService {

    private VuelosManager manager;

    public VuelosService() {
        this.manager = VuelosManagerImpl.getInstance();

        if (manager.avionesSize() == 0) {
            manager.addAvion("A1", "A320", "Vueling");
            manager.addAvion("A2", "B737", "Iberia");
            manager.addVuelo("V1", new Date(),
                    new Date(System.currentTimeMillis() + 3600000),
                    "A1", "BCN", "MAD");
        }
    }

    // -------- AVIONES --------

    @GET
    @ApiOperation(value = "Listar todos los aviones")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response = Avion.class, responseContainer="List"),
    })
    @Path("/aviones")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAviones() {

        List<Avion> aviones = this.manager.getAllAviones();

        GenericEntity<List<Avion>> entity = new GenericEntity<List<Avion>>(aviones) {};
        return Response.status(201).entity(entity).build()  ;
    }

    @GET
    @ApiOperation(value = "Obtener un avión por ID")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful", response = Avion.class),
            @ApiResponse(code = 404, message = "Avión no encontrado")
    })
    @Path("/aviones/{id}")
    public Response getAvion(@PathParam("id") String id) {
        Avion a = manager.getAvion(id);
        if (a == null) return Response.status(404).build();
        else return Response.status(201).entity(a).build();
    }

    @POST
    @ApiOperation(value = "Crear un avión")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful", response=Avion.class),
            @ApiResponse(code = 500, message = "Validation Error")
    })
    @Path("/aviones")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addAvion(Avion avion) {
        if (avion == null || avion.getId() == null)
            return Response.status(500).entity(avion).build();

        manager.addAvion(avion.getId(), avion.getModelo(), avion.getCompañia());
        return Response.status(201).entity(avion).build();
    }

    @PUT
    @ApiOperation(value = "Actualizar un avión")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful"),
            @ApiResponse(code = 404, message = "Avión no encontrado")
    })
    @Path("/aviones")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAvion(Avion avion) {
        Avion a = this.manager.updateAvion(avion);
        if (a == null)
            return Response.status(404).build();
        return Response.status(201).build();
    }


    // -------- VUELOS --------

    @GET
    @ApiOperation(value = "Listar todos los vuelos")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response = Vuelo.class, responseContainer="List"),
    })
    @Path("/vuelos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllVuelos() {
        List<Vuelo> vuelos = this.manager.getAllVuelos();
        GenericEntity<List<Vuelo>> entity = new GenericEntity<List<Vuelo>>(vuelos) {};
        return Response.status(201).entity(entity).build();
    }

    @GET
    @ApiOperation(value = "Obtener un vuelo por ID")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful", response = Vuelo.class),
            @ApiResponse(code = 404, message = "Vuelo no encontrado")
    })
    @Path("/vuelos/{id}")
    public Response getVuelo(@PathParam("id") String id) {
        Vuelo v = manager.getVuelo(id);
        if (v == null)
            return Response.status(404).build();
        else
            return Response.status(201).entity(v).build();
    }

    @POST
    @ApiOperation(value = "Crear un vuelo")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful", response=Vuelo.class),
            @ApiResponse(code = 500, message = "Validation Error")
    })
    @Path("/vuelos")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addVuelo(Vuelo vuelo) {
        if (vuelo == null || vuelo.getId() == null)
            return Response.status(500).entity(vuelo).build();
        if (vuelo.getAvionId() == null || manager.getAvion(vuelo.getAvionId()) == null) {
            return Response.status(500).entity(vuelo).build();
        }
        if (vuelo.getHoraSalida() == null || vuelo.getHoraLlegada() == null
                || !vuelo.getHoraLlegada().after(vuelo.getHoraSalida())) {
            return Response.status(500).entity(vuelo).build();
        }
        manager.addVuelo(
                vuelo.getId(),
                vuelo.getHoraSalida(),
                vuelo.getHoraLlegada(),
                vuelo.getAvionId(),
                vuelo.getOrigen(),
                vuelo.getDestino()
        );
        return Response.status(201).entity(vuelo).build();
    }

    @PUT
    @ApiOperation(value = "Actualizar un vuelo")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful"),
            @ApiResponse(code = 404, message = "Vuelo no encontrado")
    })
    @Path("/vuelos")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateVuelo(Vuelo vuelo) {
        Vuelo v = this.manager.updateVuelo(vuelo);
        if (v == null)
            return Response.status(404).build();
        return Response.status(201).build();
    }


    // -------- MALETAS --------

    @POST
    @ApiOperation(value = "Facturar una maleta en un vuelo")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful"),
            @ApiResponse(code = 400, message = "MAleta no existe"),
            @ApiResponse(code = 404, message = "Vuelo no encontrado")
    })
    @Path("/{flightId}/maletas")
    public Response facturarMaleta(@PathParam("flightId") String flightId, Maleta maleta) {
        if (maleta == null || maleta.getId() == null)
            return Response.status(400).build();
        if (manager.getVuelo(flightId) == null)
            return Response.status(404).build();
        manager.facturarMaletaUsuario(flightId, maleta);
        return Response.status(201).build();
    }

    @GET
    @ApiOperation(value = "Listar las maletas facturadas de un vuelo")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful", response = Maleta.class, responseContainer="List"),
            @ApiResponse(code = 404, message = "Vuelo no encontrado")
    })
    @Path("/{flightId}/maletas")
    public Response getMaletas(@PathParam("flightId") String flightId) {
        Vuelo v = manager.getVuelo(flightId);
        if (v == null)
            return Response.status(404).build();
        List<Maleta> maletas = this.manager.getMaletasFacturadas(flightId);
        GenericEntity<List<Maleta>> entity = new GenericEntity<List<Maleta>>(maletas) {};
        return Response.status(201).entity(entity).build()  ;
    }
}
