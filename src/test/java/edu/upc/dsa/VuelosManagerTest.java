package edu.upc.dsa;

import edu.upc.dsa.models.Vuelo;
import edu.upc.dsa.models.Avion;
import edu.upc.dsa.models.Maleta;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class AvionManagerTest {

    VuelosManager pm;

    @Before
    public void setUp() {
        pm = VuelosManagerImpl.getInstance();
        pm.addProduct("P1", "Café", 1.5);
        pm.addProduct("P2", "Bocadillo", 3.0);
        pm.addProduct("P3", "Donut", 2.0);

        Maleta maleta = new Maleta("U1");
        maleta.setName("Marc");
        pm.getUser("U1"); // asegura que el usuario se crea si no existe
    }

    @After
    public void tearDown() {
        // Como es un Singleton, limpiamos datos al final
        ((VuelosManagerImpl) pm).clear();
    }

    // ------------------------------------------------------------
    // TEST 1: Comprobamos que los productos se añaden correctamente
    // ------------------------------------------------------------
    @Test
    public void addProductTest() {
        Avion p = pm.getProduct("P1");
        Assert.assertNotNull(p);
        Assert.assertEquals("Café", p.getName());
        Assert.assertEquals(1.5, p.getPrice(), 0.001);
    }

    // ------------------------------------------------------------
    // TEST 2: Realizar un pedido (añadir a la cola)
    // ------------------------------------------------------------
    @Test
    public void createOrderTest() {
        // Crear pedido para el usuario U1
        Vuelo avion = new Vuelo("O1", "U1");
        avion.addProductList(2, "P1");
        avion.addProductList(1, "P3");

        pm.addOrder(avion);

        // Comprobar que hay un pedido pendiente
        Assert.assertEquals(1, pm.numOrders());

        // Comprobar que el pedido pertenece al usuario correcto
        Maleta u = pm.getUser("U1");
        Assert.assertNotNull(u);
        Assert.assertEquals(1, u.getOrderList().size());
        Assert.assertEquals("O1", u.getOrderList().getFirst().getId());
    }

    @Test
    public void addOrder_createsUserIfNotExistsTest() {
        ((VuelosManagerImpl) pm).clear();

        // Añadimos productos disponibles
        pm.addProduct("P1", "Café", 1.50);
        pm.addProduct("P2", "Donut", 2.00);

        // Crear un pedido con un usuario que NO está en el sistema
        Vuelo avion = new Vuelo("O1", "U123"); // U123 no existe aún
        avion.addProductList(1, "P1");
        avion.addProductList(2, "P2");

        // Confirmar que el usuario no existe antes
        Maleta preMaleta = pm.getUser("U123");
        Assert.assertNull("El usuario no debería existir antes del pedido", preMaleta);

        // Añadir el pedido (esto debe crear el usuario automáticamente)
        pm.addOrder(avion);

        // Comprobar que el pedido está en cola y que el usuario se ha creado
        Assert.assertEquals("Debe haber 1 pedido pendiente", 1, pm.numOrders());

        Maleta postMaleta = pm.getUser("U123");
        Assert.assertNotNull("El usuario debe haberse creado al añadir el pedido", postMaleta);
        Assert.assertEquals("El ID del usuario debe coincidir", "U123", postMaleta.getId());

        // Verificar que el usuario tiene el pedido en su historial
        Assert.assertEquals("El usuario debe tener 1 pedido asociado", 1, postMaleta.getOrderList().size());
        Assert.assertEquals("O1", postMaleta.getOrderList().getFirst().getId());
    }


    // ------------------------------------------------------------
    // TEST 3: Servir un pedido (FIFO) y actualizar ventas
    // ------------------------------------------------------------
    @Test
    public void deliverOrderTest() {
        // Pedido 1
        Vuelo o1 = new Vuelo("O1", "U1");
        o1.addProductList(2, "P1");  // 2 cafés
        o1.addProductList(1, "P3");  // 1 donut
        pm.addOrder(o1);

        // Pedido 2
        Vuelo o2 = new Vuelo("O2", "U1");
        o2.addProductList(3, "P2");  // 3 bocadillos
        pm.addOrder(o2);

        // Al principio hay 2 pedidos en cola
        Assert.assertEquals(2, pm.numOrders());

        // Servimos el primero
        Vuelo served = pm.deliverOrder();
        Assert.assertNotNull(served);
        Assert.assertEquals("O1", served.getId());

        // Queda 1 pedido en cola
        Assert.assertEquals(1, pm.numOrders());

        // Comprobamos que se han actualizado las ventas
        Avion p1 = pm.getProduct("P1"); // Café
        Avion p2 = pm.getProduct("P2"); // Bocadillo
        Avion p3 = pm.getProduct("P3"); // Donut

        Assert.assertEquals(2, p1.getSales());
        Assert.assertEquals(0, p2.getSales());
        Assert.assertEquals(1, p3.getSales());

        // Servimos el segundo pedido
        pm.deliverOrder();
        Assert.assertEquals(0, pm.numOrders());

        // Comprobamos ventas finales
        Assert.assertEquals(2, p1.getSales());
        Assert.assertEquals(3, p2.getSales());
        Assert.assertEquals(1, p3.getSales());
    }

    // ------------------------------------------------------------
    // TEST 4: Productos ordenados por precio y por ventas
    // ------------------------------------------------------------
    @Test
    public void getProductsOrderedTest() {
        // --- Preparar datos ---
        ((VuelosManagerImpl) pm).clear();
        pm.addProduct("P1", "Café", 1.50);
        pm.addProduct("P2", "Bocadillo", 3.00);
        pm.addProduct("P3", "Donut", 2.00);

        // Simular ventas
        Avion p1 = pm.getProduct("P1");
        Avion p2 = pm.getProduct("P2");
        Avion p3 = pm.getProduct("P3");

        p1.setSales(5);  // Café: 5 ventas
        p2.setSales(2);  // Bocadillo: 2 ventas
        p3.setSales(10); // Donut: 10 ventas

        // --- Test 1: getProductsByPrice (ascendente) ---
        List<Avion> byPrice = pm.getProductsByPrice();

        Assert.assertEquals("Café", byPrice.get(0).getName());       // 1.50
        Assert.assertEquals("Donut", byPrice.get(1).getName());      // 2.00
        Assert.assertEquals("Bocadillo", byPrice.get(2).getName());  // 3.00

        // --- Test 2: getProductsBySales (descendente) ---
        List<Avion> bySales = pm.getProductsBySales();

        Assert.assertEquals("Donut", bySales.get(0).getName());      // 10 ventas
        Assert.assertEquals("Café", bySales.get(1).getName());       // 5 ventas
        Assert.assertEquals("Bocadillo", bySales.get(2).getName());  // 2 ventas

        // --- Verificación final ---
        Assert.assertEquals(3, bySales.size());
        Assert.assertEquals(3, byPrice.size());
    }

    // ------------------------------------------------------------
    // TEST 5: Listar pedidos de un usuario
    // ------------------------------------------------------------

    @Test
    public void getServedOrdersByUserTest() {
        // Limpiar antes de empezar
        ((VuelosManagerImpl) pm).clear();

        // Añadir productos al sistema
        pm.addProduct("P1", "Café", 1.50);
        pm.addProduct("P2", "Donut", 2.00);
        pm.addProduct("P3", "Bocadillo", 3.00);

        // Crear usuario U1 con dos pedidos
        Vuelo avion1 = new Vuelo("O1", "U1");
        avion1.addProductList(2, "P1");
        avion1.addProductList(1, "P2");

        Vuelo avion2 = new Vuelo("O2", "U1");
        avion2.addProductList(1, "P3");

        // Crear otro usuario U2 con un pedido
        Vuelo avion3 = new Vuelo("O3", "U2");
        avion3.addProductList(1, "P1");

        // Añadir los pedidos
        pm.addOrder(avion1);
        pm.addOrder(avion2);
        pm.addOrder(avion3);

        // Servir los pedidos (FIFO)
        pm.deliverOrder();  // Sirve O1 (U1)
        pm.deliverOrder();  // Sirve O2 (U1)
        pm.deliverOrder();  // Sirve O3 (U2)

        // -------------------------------
        // 🔍 Comprobaciones
        // -------------------------------

        // Obtener pedidos servidos del usuario U1
        List<Vuelo> servedU1 = pm.getServedOrdersByUser("U1");

        // Comprobamos que hay 2 pedidos servidos
        Assert.assertEquals("El usuario U1 debe tener 2 pedidos servidos", 2, servedU1.size());
        Assert.assertEquals("O1", servedU1.get(0).getId());
        Assert.assertEquals("O2", servedU1.get(1).getId());

        // Obtener pedidos servidos del usuario U2
        List<Vuelo> servedU2 = pm.getServedOrdersByUser("U2");
        Assert.assertEquals("El usuario U2 debe tener 1 pedido servido", 1, servedU2.size());
        Assert.assertEquals("O3", servedU2.get(0).getId());

        // Obtener pedidos servidos de un usuario inexistente
        List<Vuelo> servedU3 = pm.getServedOrdersByUser("U3");
        Assert.assertTrue("El usuario U3 no debe tener pedidos servidos", servedU3.isEmpty());
    }
}
