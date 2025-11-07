package edu.upc.dsa;

import edu.upc.dsa.models.Libro;
import edu.upc.dsa.models.Lector;
import edu.upc.dsa.models.Prestamo;

import java.util.List;

public interface LibrosManager {
    public void addLector(String id, String nombre, String apellidos, String dni, String fechaNacimiento, String lugarNacimiento, String direccion);
    public void almacenarLibro(Libro libro);
    public Libro catalogarSiguienteLibro ();
    public void prestarLibro(Prestamo prestamo);
    public List<Prestamo> prestamosDeLector(String lectorId);
    public void clear();
    public Lector getLector(String id);
    public int lectoresSize();
    public Libro getLibroPorIsbn(String isbn);
    public int getStockPorIsbn(String isbn);
}
