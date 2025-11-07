package edu.upc.dsa;

import edu.upc.dsa.models.Lector;
import edu.upc.dsa.models.Libro;
import edu.upc.dsa.models.Prestamo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class LibrosManagerTest {
    private LibrosManager manager;

    @Before
    public void setUp() {
        manager = LibrosManagerImpl.getInstance();

        // ----- Lectores -----
        manager.addLector("L1", "Pau", "Garcia", "123A", "12/12/2004", "Barcelona", "Carrer 1");
        manager.addLector("L2", "Nil", "Miralles", "123B", "06/12/2004", "Barcelona", "Carrer 2");
        manager.addLector("L3", "Mar", "Pons", "123C", "27/03/2004", "Barcelona", "Carrer 3");

        // ----- Libros almacenados -----
        manager.almacenarLibro(new Libro("B1", "ISBN-1", "El Quijote", "Planeta", 2000, 1, "Cervantes", "Novela"));
        manager.almacenarLibro(new Libro("B2", "ISBN-2", "Cien años de soledad", "Sudamericana", 2005, 1, "G.G. Márquez", "Realismo mágico"));
        manager.almacenarLibro(new Libro("B3", "ISBN-1", "El Quijote", "Planeta", 2000, 1, "Cervantes", "Novela")); // mismo ISBN que B1
        manager.almacenarLibro(new Libro("B4", "ISBN-3", "1984", "Secker", 1949, 1, "G. Orwell", "Distopía"));
        manager.almacenarLibro(new Libro("B5", "ISBN-4", "El Hobbit", "Minotauro", 1937, 1, "Tolkien", "Fantasía"));
    }

    @After
    public void tearDown() {
        manager.clear();
    }

    // ---------------- TESTS ----------------

    @Test
    public void testAddLectorNuevo_ok() {
        manager.addLector("L4", "Joan", "Ribas", "999Z", "01/01/2000", "Girona", "Av. 1");
        Assert.assertEquals(4, manager.lectoresSize());
        Lector l4 = manager.getLector("L4");
        Assert.assertNotNull(l4);
        Assert.assertEquals("Joan", l4.getNombre());
    }

    @Test
    public void testAddLectorMismoId_mismosParametros_noCambia() {
        manager.addLector("L1", "Pau", "Garcia", "123A", "12/12/2004", "Barcelona", "Carrer 1");
        Assert.assertEquals(3, manager.lectoresSize());
        Lector l1 = manager.getLector("L1");
        Assert.assertEquals("Pau", l1.getNombre());
        Assert.assertEquals("Garcia", l1.getApellidos());
    }

    @Test
    public void testAddLectorMismoId_parametrosDistintos_actualiza() {
        manager.addLector("L1", "Pauet", "García", "123A", "12/12/2004", "Barcelona", "Carrer 2");
        Lector l1 = manager.getLector("L1");
        Assert.assertEquals("Pauet", l1.getNombre());
        Assert.assertEquals("Carrer 2", l1.getDireccion());
    }

    @Test
    public void testCatalogar_acumulaStockPorIsbn() {
        for (int i = 0; i < 5; i++) {
            manager.catalogarSiguienteLibro(); // ORDEN: B5-B4-B3-B2-B1
        }
        Assert.assertEquals(2, manager.getStockPorIsbn("ISBN-1"));
        Assert.assertEquals(1, manager.getStockPorIsbn("ISBN-2"));
        Assert.assertEquals(1, manager.getStockPorIsbn("ISBN-3"));
        Assert.assertEquals(1, manager.getStockPorIsbn("ISBN-4"));
    }

    @Test
    public void testCatalogar_creaDosMontones_ySigueOrdenFIFO() {
        // Voy a crear 11 libros del mismo ISBN para que así se creen 2 montones
        for (int i = 1; i <= 11; i++) {
            manager.almacenarLibro(new Libro("B6" + i, "ISBN-6", "Dune", "Ace", 1965, 1, "Frank Herbert", "Ciencia ficción"));
        }
        for (int i = 0; i < 16; i++) {
            manager.catalogarSiguienteLibro();
        }
        Assert.assertEquals(11, manager.getStockPorIsbn("ISBN-6"));
    }

    @Test
    public void testPrestarLibro_porId_ok_decrementaStockDelIsbn() {
        for (int i = 0; i < 5; i++) {
            manager.catalogarSiguienteLibro();  // Se catalogan los 5 libros del setup
        }
        Assert.assertEquals(2, manager.getStockPorIsbn("ISBN-1"));
        Prestamo p = new Prestamo("PR1", "L1", "B1", "01/01/2025", "15/01/2025");
        manager.prestarLibro(p);
        Assert.assertEquals(1, manager.getStockPorIsbn("ISBN-1"));
        Assert.assertNotNull(manager.getLibroPorIsbn("ISBN-1"));
        List<Prestamo> prestamosL1 = manager.prestamosDeLector("L1");
        Assert.assertEquals(1, prestamosL1.size());
        Assert.assertTrue(prestamosL1.get(0).isEnTramite());
        Assert.assertEquals("B1", prestamosL1.get(0).getLibroId());
    }

    @Test
    public void testPrestarLibro_sinStock_noAñadeSegundoPrestamo() {
        for (int i = 0; i < 5; i++) {
            manager.catalogarSiguienteLibro();
        }
        manager.almacenarLibro(new Libro("B7", "ISBN-7", "Fahrenheit 451", "Simon & Schuster", 1953, 1, "Ray Bradbury", "Ciencia ficción"));
        manager.catalogarSiguienteLibro();
        Assert.assertEquals(1, manager.getStockPorIsbn("ISBN-7"));
        manager.prestarLibro(new Prestamo("PU1", "L2", "B7", "01/02/2025", "07/02/2025"));
        Assert.assertEquals(0, manager.getStockPorIsbn("ISBN-7"));
        manager.prestarLibro(new Prestamo("PU2", "L2", "B7", "02/02/2025", "08/02/2025"));
        Assert.assertEquals(0, manager.getStockPorIsbn("ISBN-7"));
        List<Prestamo> pL2 = manager.prestamosDeLector("L2");
        Assert.assertEquals(1, pL2.size());
        Assert.assertEquals("PU1", pL2.get(0).getId());
    }

    @Test
    public void testPrestamosDeLector_sinPrestamos_listaVacia() {
        List<Prestamo> prestamos = manager.prestamosDeLector("L3");
        Assert.assertNotNull(prestamos);
        Assert.assertTrue(prestamos.isEmpty());
    }

    @Test
    public void testClear() {
        // Catalogamos algunos y prestamos uno
        for (int i = 0; i < 3; i++) {
            manager.catalogarSiguienteLibro();
        }
        manager.prestarLibro(new Prestamo("PC1", "L1", "B1", "01/03/2025", "10/03/2025"));
        manager.clear();
        Assert.assertEquals(0, manager.lectoresSize());
        Assert.assertNull(manager.getLector("L1"));
        Assert.assertEquals(0, manager.getStockPorIsbn("ISBN-1"));
        List<Prestamo> prestamos = manager.prestamosDeLector("L1");
        Assert.assertTrue(prestamos.isEmpty());
    }
}
