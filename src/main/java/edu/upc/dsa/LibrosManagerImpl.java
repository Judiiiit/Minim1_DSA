package edu.upc.dsa;

import edu.upc.dsa.models.Prestamo;
import edu.upc.dsa.models.Libro;
import edu.upc.dsa.models.Lector;
import edu.upc.dsa.exceptions.*;

import java.util.*;

import org.apache.log4j.Logger;

public class LibrosManagerImpl implements LibrosManager {
    private HashMap<String, Lector> lectores;
    private Queue<Stack<Libro>> montonesLibros;
    private int numLibrosMaxPorMonton = 10;
    private HashMap<String, Libro> catalogoLibrosPorIsbn;
    private HashMap<String, Libro> catalogoLibrosPorId;
    private HashMap<String, Prestamo> prestamos;

    final static Logger logger = Logger.getLogger(LibrosManagerImpl.class);

    private static LibrosManager instance;

    private LibrosManagerImpl() {
        this.lectores = new HashMap<>();
        this.montonesLibros = new LinkedList<>();
        this.catalogoLibrosPorIsbn = new HashMap<>();
        this.catalogoLibrosPorId = new HashMap<>();
        this.prestamos = new HashMap<>();
    }

    public static LibrosManager getInstance() {
        if (instance==null) instance = new LibrosManagerImpl();
        return instance;
    }

    @Override
    public void addLector(String id, String nombre, String apellidos, String dni, String fechaNacimiento, String lugarNacimiento, String direccion) {
        logger.info("Añadiendo lector: id = " + id + ", nombre = " + nombre + ", apellidos = " + apellidos + ", dni = " + dni + " fecha de nacimiento = " + fechaNacimiento + ", lugar de nacimiento = " + lugarNacimiento + ", direccion = " + direccion);
        try {
            if (lectores.containsKey(id)) {
                Lector lector = lectores.get(id);
                if (lector.getNombre().equals(nombre) && lector.getApellidos().equals(apellidos) && lector.getDni().equals(dni) && lector.getFechaNacimiento().equals(fechaNacimiento) && lector.getLugarNacimiento().equals(lugarNacimiento) && lector.getDireccion().equals(direccion)) {
                    logger.error("No se pueden usar los mismos valores de nombre, apellidos, dni, fecha de nacimiento, lugar de nacimiento y dirección para un ID existente");
                    throw new MismosParametrosPorIdException("El lector con ID " + id + " ya tiene esos valores.");
                }
                else {
                    lector.setNombre(nombre);
                    lector.setApellidos(apellidos);
                    lector.setDni(dni);
                    lector.setFechaNacimiento(fechaNacimiento);
                    lector.setLugarNacimiento(lugarNacimiento);
                    lector.setDireccion(direccion);
                    logger.info("Lector actualizado: " + lector);
                }
            }
            else {
                Lector lector = new Lector(id, nombre, apellidos, dni, fechaNacimiento, lugarNacimiento, direccion);
                lectores.put(id, lector);
                logger.info("Lector añadido: " + lector);
            }
        }
        catch (MismosParametrosPorIdException ex) {
            logger.error("Excepción mismos parametros con mismo id: ", ex);
        }
    }

    @Override
    public void almacenarLibro(Libro libro) {
        logger.info("Almacenando libro: idLibro = " + libro.getId() + ", isbn = " + libro.getIsbn() + ", titulo = " + libro.getTitulo() + ", editorial = " +  libro.getEditorial() + ", año de publicación = " + libro.getAñoPublicacion() + ", edición = " + libro.getEdicion() + ", autor = " + libro.getAutor() + ", tematica = " + libro.getTematica());
        try {
            if (libro == null || libro.getId() == null) {
                logger.error("Error: el libro con id " + libro.getId() + " no existe");
                throw new LibroNotFoundException("El libro con id " + libro.getId() + " no existe");
            }
            Stack<Libro> ultimo = null;
            for (Stack<Libro> m : montonesLibros) {
                ultimo = m;
            }
            if (ultimo == null || ultimo.size() >= numLibrosMaxPorMonton) {
                if (ultimo != null && ultimo.size() >= numLibrosMaxPorMonton) {
                    logger.info("Último montón lleno (" + ultimo.size() + "/" + numLibrosMaxPorMonton + ")");
                }
                ultimo = new Stack<>();
                montonesLibros.add(ultimo);
                logger.info("Creado nuevo montón. Ahora el número de montones = " + montonesLibros.size());
            }
            ultimo.push(libro);
            logger.info("Libro apilado. Tamaño del último montón = " + ultimo.size());
        }
        catch (LibroNotFoundException ex) {
            logger.error("Excepción en almacenarLibro: ", ex);
        }
    }

    @Override
    public Libro catalogarSiguienteLibro () {
        logger.info("Catalogando siguiente libro (el libro corresponde al q ocupa la posición: primer montón de la cola, desapilando por arriba)");
        try {
            if (montonesLibros.isEmpty()) {
                logger.error("No hay libros pendientes de catalogar");
                throw new NoHayLibrosPorCatalogarException("No hay libros pendientes de catalogar");
            }
            Stack<Libro> primerMonton = montonesLibros.peek();
            Libro libroACatalogar = primerMonton.pop();

            if (primerMonton.isEmpty()) {
                montonesLibros.poll();
                logger.info("Primer montón se quedó vacío. Eliminado de la cola.");
            }
            String isbn = libroACatalogar.getIsbn();
            Libro libroYaExistente = catalogoLibrosPorIsbn.get(isbn);

            if (libroYaExistente == null) {
                libroACatalogar.setNumEjemplares(1);
                catalogoLibrosPorIsbn.put(isbn, libroACatalogar);
                logger.info("Nuevo ISBN en catálogo. Ejemplares = 1. " + libroACatalogar);
            }
            else {
                libroYaExistente.aumentarNumEjemplares();
                logger.info("ISBN ya existente. Incrementando ejemplares a " + libroYaExistente.getNumEjemplares() + " del libro " + libroYaExistente.getId() + " con título " + libroYaExistente.getTitulo());
            }
            catalogoLibrosPorId.put(libroACatalogar.getId(), libroACatalogar);
            return catalogoLibrosPorIsbn.get(isbn);
        }
        catch (NoHayLibrosPorCatalogarException ex) {
            logger.error("Excepción en catalogarSiguienteLibro: ", ex);
            return null;
        }
    }

    @Override
    public void prestarLibro(Prestamo prestamo) {
        logger.info("Creando préstamo: " + prestamo);
        try {
            if (prestamo == null || prestamo.getId() == null) {
                logger.error("El préstamo con id " + prestamo.getId() + " no existe");
                throw new PrestamoNotFoundException("El préstamo con id " + prestamo.getId() + " no existe");
            }
            String lectorId = prestamo.getLectorId();
            String libroId = prestamo.getLibroId();

            Lector lector = lectores.get(lectorId);
            if (lector == null) {
                logger.error("El lector con id " + lectorId + " no existe");
                throw new LectorNotFoundException("El lector con id " + lectorId + " no existe");
            }
            Libro ejemplar = catalogoLibrosPorId.get(libroId);
            if (ejemplar == null) {
                logger.error("El ejemplar con id " + libroId + " no existe o no está catalogado");
                throw new LibroNotFoundException("El ejemplar con id " + libroId + " no existe o no está catalogado");
            }
            Libro agregadoPorIsbn = catalogoLibrosPorIsbn.get(ejemplar.getIsbn());
            if (agregadoPorIsbn == null) {
                logger.error("Inconsistencia: ISBN " + ejemplar.getIsbn() + " no está en el catálogo");
                throw new LibroNotFoundException("ISBN no encontrado en catálogo");
            }
            if (agregadoPorIsbn.getNumEjemplares() <= 0) {
                logger.error("Sin ejemplares disponibles para ISBN " + ejemplar.getIsbn());
                throw new SinEjemplaresDisponiblesException("Sin ejemplares disponibles para ISBN " + ejemplar.getIsbn());
            }
            agregadoPorIsbn.decrementarNumEjemplares();
            prestamo.setEnTramite(true);
            prestamos.put(prestamo.getId(), prestamo);
            logger.info("Préstamo " + prestamo.getId() + " creado. Stock ISBN " + ejemplar.getIsbn() + " = " + agregadoPorIsbn.getNumEjemplares());
        }
        catch (PrestamoNotFoundException | LectorNotFoundException | LibroNotFoundException | SinEjemplaresDisponiblesException ex) {
            logger.error("Excepción en prestarLibro: ", ex);
        }
    }

    @Override
    public List<Prestamo> prestamosDeLector(String lectorId) {
        logger.info("Obteniendo préstamos del lector con id " + lectorId);
        try {
            List<Prestamo> prestamosLector = new ArrayList<>();

            for (Prestamo p : prestamos.values()) {
                if (p.getLectorId().equals(lectorId)) {
                    prestamosLector.add(p);
                }
            }
            if (prestamosLector.isEmpty()) {
                logger.error("No se encontraron préstamos para el lector con id " + lectorId);
                throw new PrestamoNotFoundException("No se encontraron préstamos para el lector con id " + lectorId);
            }
            else {
                logger.info("Se encontraron " + prestamosLector.size() + " préstamos para el lector con id " + lectorId);
            }
            return prestamosLector;
        } catch (Exception ex) {
            logger.error("Excepción en prestamosDeLector: ", ex);
            return new ArrayList<>();
        }
    }
    @Override
    public void clear() {
        logger.info("clear(): start");
        montonesLibros.clear();
        lectores.clear();
        catalogoLibrosPorIsbn.clear();
        catalogoLibrosPorId.clear();
        logger.info("clear(): end");
    }

    @Override
    public int lectoresSize() {
        int ret = this.lectores.size();
        logger.info("size " + ret);
        return ret;
    }

    @Override
    public Lector getLector(String id) {
        logger.info("Buscando lector con id = " + id);
        try {
            if (!lectores.containsKey(id)) {
                logger.error("El lector con id " + id + " no existe");
                throw new LectorNotFoundException("El lector con id " + id + " no existe");
            }
            return lectores.get(id);
        }
        catch (LectorNotFoundException ex) {
            logger.error("Excepción buscar lector: ", ex);
            return null;
        }
    }

    @Override
    public Libro getLibroPorIsbn(String isbn) {
        return catalogoLibrosPorIsbn.get(isbn);
    }

    @Override
    public int getStockPorIsbn(String isbn) {
        Libro l = catalogoLibrosPorIsbn.get(isbn);
        if (l != null) {
            return l.getNumEjemplares();
        }
        else {
            return 0;
        }
    }

}