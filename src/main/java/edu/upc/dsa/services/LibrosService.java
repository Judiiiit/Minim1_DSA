package edu.upc.dsa.services;

import edu.upc.dsa.LibrosManager;
import edu.upc.dsa.LibrosManagerImpl;
import edu.upc.dsa.models.Lector;
import edu.upc.dsa.models.Libro;
import edu.upc.dsa.models.Prestamo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;

@Api(value = "/librosManager", description = "Endpoint to manage lectores, libros and préstamos")
@Path("/librosManager")

public class LibrosService {

    private LibrosManager manager;

    public LibrosService() {
        this.manager = LibrosManagerImpl.getInstance();

        if (manager.lectoresSize() == 0) {
            if (manager.lectoresSize() == 0) {
                // ---- LECTORES ----
                manager.addLector("L1", "Pau", "Garcia", "123456789A", "12/12/2004", "Barcelona", "Carrer n1");
                manager.addLector("L2", "Nil", "Miralles", "123456789B", "06/12/2004", "Barcelona", "Carrer n2");
                manager.addLector("L3", "Mar", "Pons", "123456789C", "27/03/2004", "Barcelona", "Carrer n3");

                // ---- LIBROS EN ALMACÉN (para catalogar) ----
                manager.almacenarLibro(new Libro("B1", "9788491050295", "El Quijote", "Planeta", 2005, 1, "Miguel de Cervantes", "Novela clásica"));
                manager.almacenarLibro(new Libro("B2", "9788437604947", "Cien años de soledad", "Sudamericana", 1967, 1, "Gabriel García Márquez", "Realismo mágico"));
                manager.almacenarLibro(new Libro("B3", "9788499890947", "1984", "Secker & Warburg", 1949, 1, "George Orwell", "Distopía"));
                manager.almacenarLibro(new Libro("B4", "9788445077568", "El Hobbit", "Minotauro", 1937, 1, "J.R.R. Tolkien", "Fantasía"));

                // ---- CATALOGAR UNO PARA que haya stock disponible ----
                manager.catalogarSiguienteLibro(); // Catalogado B4

                // ---- Crear un préstamo de ejemplo ----
                Prestamo p = new Prestamo("PRES1", "L1", "B4", "01/11/2025", "15/11/2025");
                manager.prestarLibro(p);
            }
        }
    }

    // -------- RUTES --------

    @POST
    @ApiOperation(value = "Añadir un lector")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful", response=Lector.class),
            @ApiResponse(code = 500, message = "Validation Error")
    })
    @Path("/lectores")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addLector(Lector lector) {
        if (lector == null || lector.getId() == null) {
            return Response.status(500).entity(lector).build();
        }
        manager.addLector(lector.getId(), lector.getNombre(), lector.getApellidos(), lector.getDni(), lector.getFechaNacimiento(), lector.getLugarNacimiento(), lector.getDireccion());
        return Response.status(201).entity(lector).build();
    }

    @POST
    @ApiOperation(value = "Almacenar un libro")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful", response = Libro.class),
            @ApiResponse(code = 404, message = "Libro no encontrado")
    })
    @Path("/libros/almacen")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response almacenarLibro(Libro libro) {
        if (libro == null || libro.getId() == null) {
            return Response.status(404).build();
        }
        manager.almacenarLibro(libro);
        return Response.status(201).entity(libro).build();
    }

    @POST
    @ApiOperation(value = "Catalogar siguiente libro")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful"),
            @ApiResponse(code = 404, message = "No hay libros pendientes de catalogar")
    })
    @Path("/libros/catalogo")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response catalogarSiguiente() {
        Libro libro = manager.catalogarSiguienteLibro();
        if (libro == null) {
            return Response.status(404).build();
        }
        return Response.status(201).entity(libro).build();
    }

    @POST
    @ApiOperation(value = "Crear un préstamo (por id de lector e id de ejemplar catalogado)")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Préstamo creado", response = Prestamo.class),
            @ApiResponse(code = 404, message = "Petición inválida")
    })
    @Path("/prestamos")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response prestarLibro(Prestamo prestamo) {
        if (prestamo == null || prestamo.getId() == null) {
            return Response.status(400).build();
        }
        manager.prestarLibro(prestamo);
        return Response.status(201).entity(prestamo).build();
    }

    @GET
    @ApiOperation(value = "Listar todos los préstamos de un lector")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successful", response = Prestamo.class, responseContainer="List")
    })
    @Path("/prestamos/{lectorId}")
    public Response getPrestamosLector(@PathParam("lectorId") String lectorId) {
        List<Prestamo> list = manager.prestamosDeLector(lectorId);
        GenericEntity<List<Prestamo>> entity = new GenericEntity<List<Prestamo>>(list) {};
        return Response.status(201).entity(entity).build();
    }

}
