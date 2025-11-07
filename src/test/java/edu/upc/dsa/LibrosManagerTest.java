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
        manager.clear();
        manager.addLector("Lector1", "Pau", "Garcia", "123456789A", "12/12/2004", "Barcelona", "Carrer n1");
        manager.addLector("Lector2", "Nil", "Miralles", "123456789B", "06/12/2004", "Barcelona", "Carrer n2");
        manager.addLector("Lector3", "Mar", "Pons", "123456789C", "27/03/2004", "Barcelona", "Carrer n3");
    }
    @After
    public void tearDown() {
        manager.clear();
    }

    // ---------------- TESTS ----------------

    @Test
    public void testAddLector_yMismosParametros() {
        manager.addLector("Lector1", "Pau", "Garcia", "123456789A", "12/08/2004", "Barcelona", "Carrer n1");
    }

    @Test
    public void testAddLector() {
        Assert.assertEquals(3, manager.lectoresSize());
        Lector l3 = manager.getLector("Lector3");
        Assert.assertNotNull(l3);
        manager.addLector("Lector4", "Maria", "Jimenez", "111223432D", "15/06/2004", "Barcelona", "Carrer n4");
        Lector l4 = manager.getLector("Lector4");
        Assert.assertEquals("Maria", l4.getNombre());
        Assert.assertEquals("111223432D", l4.getDni());
    }

    @Test
    public void testAlmacenar_y_Catalogar_incrementaPorISBN() {
        // Dos libros con el mismo ibn
        manager.almacenarLibro(new Libro("B1", "ISBN-A", "Titulo A", "Ed", 2020, 1, "Autor", "Tema"));
        manager.almacenarLibro(new Libro("B2", "ISBN-A", "Titulo A", "Ed", 2020, 1, "Autor", "Tema"));

        Libro l1 = manager.catalogarSiguienteLibro();
        Assert.assertNotNull(l1);
        Assert.assertEquals("ISBN-A", l1.getIsbn());
        Assert.assertEquals(1, l1.getNumEjemplares());

        Libro l2 = manager.catalogarSiguienteLibro();
        Assert.assertNotNull(l2);
        Assert.assertEquals("ISBN-A", l2.getIsbn());
        Assert.assertEquals(2, l2.getNumEjemplares());

        Assert.assertSame(l1, l2);
    }

    @Test
    public void testCatalogarSinLibros_devuelveNull() {
        Libro l = manager.catalogarSiguienteLibro();
        Assert.assertNull(l);
    }

    @Test
    public void testPrestarLibro_OK_yDecrementaEjemplares() {
        // Añadimos al catálogo 2 ejemplares del mismo ISBN
        manager.almacenarLibro(new Libro("LB1", "ISBN-1", "T1", "Ed", 2022, 1, "Autor", "Tema"));
        manager.almacenarLibro(new Libro("LB2", "ISBN-1", "T1", "Ed", 2022, 1, "Autor", "Tema"));

        Libro can = manager.catalogarSiguienteLibro(); // ejemplares = 1
        can = manager.catalogarSiguienteLibro();       // ejemplares = 2

        Assert.assertNotNull(can);
        Assert.assertEquals(2, can.getNumEjemplares());

        // Préstamo correcto a Lector1 usando el id del canónico
        Prestamo p = new Prestamo("P1", "Lector1", can.getId(), "07/11/2025", "07/12/2025");
        manager.prestarLibro(p);

        // Decrementa a 1
        Assert.assertEquals(1, can.getNumEjemplares());

        // Y se puede consultar
        List<Prestamo> prestamos = manager.prestamosDeLector("Lector1");
        Assert.assertNotNull(prestamos);
        Assert.assertEquals(1, prestamos.size());
        Assert.assertEquals("P1", prestamos.get(0).getId());
    }

    @Test
    public void testPrestarLibro_conLectorInexistente_noCambiaEstado() {
        manager.almacenarLibro(new Libro("LB1", "ISBN-X", "TX", "Ed", 2022, 1, "A", "T"));
        Libro can = manager.catalogarSiguienteLibro();
        Assert.assertNotNull(can);
        Assert.assertEquals(1, can.getNumEjemplares());

        // Intento con lector que no existe
        Prestamo p = new Prestamo("PX", "NO-LECTOR", can.getId(), "07/11/2025", "07/12/2025");
        manager.prestarLibro(p);

        // No debe decrementar ejemplares
        Assert.assertEquals(1, can.getNumEjemplares());

        // Y la consulta del lector inexistente debe devolver lista vacía
        List<Prestamo> lista = manager.prestamosDeLector("NO-LECTOR");
        Assert.assertNotNull(lista);
        Assert.assertTrue(lista.isEmpty());
    }

    @Test
    public void testPrestarLibro_conLibroInexistente_noGuardaPrestamo() {
        // Lector válido
        // Intento prestar un libro que NO está catalogado
        Prestamo p = new Prestamo("P2", "Lector2", "NO-LIBRO", "07/11/2025", "07/12/2025");
        manager.prestarLibro(p);

        // No habrá préstamos para Lector2
        List<Prestamo> lista = manager.prestamosDeLector("Lector2");
        Assert.assertNotNull(lista);
        Assert.assertTrue(lista.isEmpty());
    }

    @Test
    public void testPrestarLibro_sinEjemplares_rechazaSegundoPrestamo() {
        manager.almacenarLibro(new Libro("LB1", "ISBN-2", "T2", "Ed", 2022, 1, "A", "T"));
        Libro can = manager.catalogarSiguienteLibro();
        Assert.assertNotNull(can);
        Assert.assertEquals(1, can.getNumEjemplares());

        // 1º préstamo OK
        manager.prestarLibro(new Prestamo("P1", "Lector3", can.getId(), "07/11/2025", "07/12/2025"));
        Assert.assertEquals(0, can.getNumEjemplares());

        // 2º préstamo (sin ejemplares) -> no debe cambiar el estado
        manager.prestarLibro(new Prestamo("P2", "Lector3", can.getId(), "07/11/2025", "07/12/2025"));
        Assert.assertEquals(0, can.getNumEjemplares());

        // Sólo debe haber 1 préstamo registrado para Lector3
        List<Prestamo> lista = manager.prestamosDeLector("Lector3");
        Assert.assertNotNull(lista);
        Assert.assertEquals(1, lista.size());
        Assert.assertEquals("P1", lista.get(0).getId());
    }

    @Test
    public void testPrestamosDeLector_conResultados() {
        manager.almacenarLibro(new Libro("LB1", "ISBN-3", "T3", "Ed", 2022, 1, "A", "T"));
        manager.almacenarLibro(new Libro("LB2", "ISBN-3", "T3", "Ed", 2022, 1, "A", "T"));
        Libro can = manager.catalogarSiguienteLibro();
        can = manager.catalogarSiguienteLibro();

        manager.prestarLibro(new Prestamo("PA", "Lector1", can.getId(), "07/11/2025", "07/12/2025"));
        manager.prestarLibro(new Prestamo("PB", "Lector1", can.getId(), "07/11/2025", "07/12/2025"));

        List<Prestamo> lista = manager.prestamosDeLector("Lector1");
        Assert.assertNotNull(lista);
        Assert.assertEquals(2, lista.size());
    }

    @Test
    public void testPrestamosDeLector_sinResultados_devuelveListaVacia() {
        List<Prestamo> lista = manager.prestamosDeLector("SIN-PRESTAMOS");
        Assert.assertNotNull(lista);
        Assert.assertTrue(lista.isEmpty());
    }


    @Test
    public void testClear() {
        manager.clear();
        Assert.assertEquals(0, manager.lectoresSize());
    }
}
