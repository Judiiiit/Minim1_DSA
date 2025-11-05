package edu.upc.dsa;

import edu.upc.dsa.models.Vuelo;
import edu.upc.dsa.models.Avion;
import edu.upc.dsa.models.Maleta;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class VuelosManagerTest {
    private VuelosManager manager;

    @Before
    public void setUp() {
        manager = VuelosManagerImpl.getInstance();
        manager.addAvion("A1", "A320", "Vueling");
        manager.addAvion("A2", "Boeing 737", "Iberia");
        manager.addAvion("A3", "Embraer 195", "Air Europa");

        Date salida = new Date();
        Date llegada = new Date(System.currentTimeMillis() + 3600000);
        manager.addVuelo("V1", salida, llegada, "A1", "BCN", "MAD");
        manager.addVuelo("V2", salida, llegada, "A2", "BCN", "PAR");
    }
    @After
    public void tearDown() {
        ((VuelosManagerImpl) manager).clear();
    }

    // ---------------- AVIONES ----------------

    @Test
    public void testGetAvionExistente() {
        Avion a = manager.getAvion("A1");
        Assert.assertNotNull(a);
        Assert.assertEquals("A320", a.getModelo());
        Assert.assertEquals("Vueling", a.getCompañia());
    }

    @Test
    public void testActualizarAvion() {
        manager.addAvion("A1", "Boeing 737", "Ryanair");
        Avion a = manager.getAvion("A1");
        Assert.assertEquals("Boeing 737", a.getModelo());
        Assert.assertEquals("Ryanair", a.getCompañia());
    }

    @Test
    public void testAddAvionMismosParametros() {
        manager.addAvion("A1", "A320", "Vueling"); // mismos datos
        Avion a = manager.getAvion("A1");
        Assert.assertEquals("A320", a.getModelo()); // No cambia
        Assert.assertEquals("Vueling", a.getCompañia());
    }

    @Test
    public void testGetAvionNoExistente() {
        Avion a = manager.getAvion("AX");
        Assert.assertNull(a);
    }

    @Test
    public void testAvionesSize() {
        int size = manager.avionesSize();
        Assert.assertEquals(3, size);
    }

    // ---------------- VUELOS ----------------

    @Test
    public void testGetVueloExistente() {
        Vuelo v = manager.getVuelo("V1");
        Assert.assertNotNull(v);
        Assert.assertEquals("A1", v.getAvionId());
        Assert.assertEquals("BCN", v.getOrigen());
        Assert.assertEquals("MAD", v.getDestino());
    }

    @Test
    public void testAddVueloNuevo() {
        Date salida = new Date();
        Date llegada = new Date(System.currentTimeMillis() + 7200000);
        manager.addVuelo("V3", salida, llegada, "A3", "BCN", "ROM");
        Vuelo v = manager.getVuelo("V3");
        Assert.assertNotNull(v);
        Assert.assertEquals("ROM", v.getDestino());
    }

    @Test
    public void testAddVueloAvionNoExiste() {
        Date salida = new Date();
        Date llegada = new Date(System.currentTimeMillis() + 3600000);
        manager.addVuelo("V99", salida, llegada, "A999", "BCN", "LIS");
        Vuelo v = manager.getVuelo("V99");
        Assert.assertNull(v);
    }

    @Test
    public void testAddVueloFechasInvalidas() {
        Date salida = new Date();
        Date llegada = new Date(System.currentTimeMillis() - 3600000); // llegada antes que salida
        manager.addVuelo("V50", salida, llegada, "A1", "BCN", "ROM");
        Vuelo v = manager.getVuelo("V50");
        Assert.assertNull(v);
    }

    @Test
    public void testAddVueloMismosParametros() {
        // Misma configuración que V1
        Date salida = new Date();
        Date llegada = new Date(System.currentTimeMillis() + 3600000);
        manager.addVuelo("V1", salida, llegada, "A1", "BCN", "MAD"); // debería loggear error
        Assert.assertEquals(2, manager.getAllVuelos().size()); // sigue habiendo solo 2
    }

    @Test
    public void testActualizarVueloExistente() {
        Vuelo v1 = manager.getVuelo("V1");
        Date nuevaSalida = new Date(System.currentTimeMillis() + 7200000);
        Date nuevaLlegada = new Date(System.currentTimeMillis() + 10800000);
        manager.addVuelo("V1", nuevaSalida, nuevaLlegada, "A1", "BCN", "BER"); // cambia destino
        Vuelo actualizado = manager.getVuelo("V1");
        Assert.assertEquals("BER", actualizado.getDestino());
        Assert.assertEquals(v1.getId(), actualizado.getId());
    }

    @Test
    public void testGetVueloNoExistente() {
        Vuelo v = manager.getVuelo("V500");
        Assert.assertNull(v);
    }

    // ---------------- MALETAS ----------------

    @Test
    public void testFacturarMaletaCorrecta() {
        Maleta m1 = new Maleta("M1");
        manager.facturarMaletaUsuario("V1", m1);
        List<Maleta> maletas = manager.getMaletasFacturadas("V1");
        Assert.assertEquals(1, maletas.size());
        Assert.assertEquals("V1", maletas.get(0).getFlightId());
    }

    @Test
    public void testFacturarMaletaVueloNoExiste() {
        Maleta m1 = new Maleta("M1");
        manager.facturarMaletaUsuario("V999", m1);
        List<Maleta> maletas = manager.getMaletasFacturadas("V999");
        Assert.assertNotNull(maletas);
        Assert.assertTrue(maletas.isEmpty());
    }

    @Test
    public void testGetMaletasFacturadasVueloNoExiste() {
        List<Maleta> maletas = manager.getMaletasFacturadas("INEXISTENTE");
        Assert.assertNotNull(maletas);
        Assert.assertTrue(maletas.isEmpty());
    }

    // ---------------- LISTADOS Y CLEAR ----------------

    @Test
    public void testGetAllAvionesYVuelos() {
        List<Avion> aviones = manager.getAllAviones();
        List<Vuelo> vuelos = manager.getAllVuelos();
        Assert.assertEquals(3, aviones.size());
        Assert.assertEquals(2, vuelos.size());
    }

    @Test
    public void testClear() {
        manager.clear();
        Assert.assertEquals(0, manager.getAllAviones().size());
        Assert.assertEquals(0, manager.getAllVuelos().size());
    }

    // ---------------- NULOS Y ERRORES ----------------

    @Test
    public void testAddVueloConFechasNull() {
        manager.addVuelo("VNULL", null, null, "A1", "BCN", "LIS");
        Vuelo v = manager.getVuelo("VNULL");
        Assert.assertNull(v);
    }

    @Test
    public void testFacturarMaletaNull() {
        try {
            manager.facturarMaletaUsuario("V1", null);
            Assert.fail("Debería lanzar NullPointerException o loggear error");
        }
        catch (Exception e) {
            Assert.assertTrue(true);
        }
    }
}
