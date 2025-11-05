package edu.upc.dsa;

import edu.upc.dsa.models.Product;
import edu.upc.dsa.models.Order;
import edu.upc.dsa.models.User;

import java.util.*;

import org.apache.log4j.Logger;

public class ProductManagerImpl implements ProductManager {
    private List<Product> productList;
    private Queue<Order> orderQueue;
    private HashMap<String, User> users;
    private List<Order> servedOrders;

    final static Logger logger = Logger.getLogger(ProductManagerImpl.class);

    private static ProductManager instance;

    public ProductManagerImpl() {
        this.productList = new ArrayList<>();
        this.orderQueue = new LinkedList<>();
        this.users = new HashMap<>();
        this.servedOrders = new ArrayList<>();
    }

    public static ProductManager getInstance() {
        if (instance==null) instance = new ProductManagerImpl();
        return instance;
    }

    @Override
    public int size() {
        int ret = this.productList.size();
        logger.info("size " + ret);
        return ret;
    }

    public List<Product> findAll() {
        return this.productList;
    }

    @Override
    public void addProduct(String id, String name, double price) {
        logger.info(String.format("addProduct(id=%s, name=%s, price=%.2f): start", id, name, price));
        if (id == null || name == null) {
            logger.error("addProduct: id o name es null");
            throw new IllegalArgumentException("id y name no pueden ser null");  // Preguntar si se puede usar el IllegalArgument o si hemos de crear excepciones
        }
        if (price < 0) {
            logger.error("addProduct: price negativo");
            throw new IllegalArgumentException("price no puede ser negativo");
        }

        productList.add(new Product(id, name, price));

        logger.info(String.format("addProduct: end (totalProducts=%d)", productList.size()));
    }

    @Override
    public List<Product> getProductsByPrice() {
        logger.info("getProductsByPrice(): start");
        List<Product> orderedProducts = new ArrayList<>();
        orderedProducts.addAll(productList);
        Collections.sort(orderedProducts, Comparator.comparingDouble(Product::getPrice));

        logger.info("getProductsByPrice(): productos ordenados por precio ascendente:");
        for (Product p : orderedProducts) {
            logger.info(" - " + p.getName() + " | Precio: " + p.getPrice() + "€ | Ventas: " + p.getSales());
        }
        logger.info("getProductsByPrice(): end (size=" + orderedProducts.size() + ")");
        return orderedProducts;
    }

    @Override
    public List<Product> getProductsBySales() {
        logger.info("getProductsBySales(): start");

        if (productList == null || productList.isEmpty()) {
            logger.warn("getProductsBySales(): la lista de productos está vacía");
            return new ArrayList<>();
        }

        // Clonar la lista para no modificar la original
        List<Product> sortedList = new ArrayList<>(productList);

        logger.info("getProductsBySales(): ordenando " + sortedList.size() + " productos por número de ventas (descendente)");

        // Ordenar descendentemente por número de ventas
        sortedList.sort((p1, p2) -> Integer.compare(p2.getSales(), p1.getSales()));

        logger.info("getProductsBySales(): productos ordenados por ventas descendente:");
        for (Product p : sortedList) {
            logger.info(" - " + p.getName() + " | Precio: " + p.getPrice() + "€ | Ventas: " + p.getSales());
        }

        return sortedList;
    }

    @Override
    public void addOrder(Order order) {
        logger.info("addOrder(orderId=" + (order != null ? order.getId() : "null") + "): start");
        if (order == null) {
            logger.error("addOrder: order es null");
            throw new IllegalArgumentException("order no puede ser null");
        }

        // Asegurar usuario en memoria
        String userId = order.getUser().getId();
        User u = users.get(userId);
        if (u == null) {
            u = order.getUser(); // viene de order.getUser() (crea si no existe)
            users.put(userId, u);
            logger.info("addOrder: nuevo usuario registrado userId=" + userId);
        }

        // Añadir el pedido a la cola FIFO y a la lista del usuario (histórico)
        orderQueue.add(order);
        u.addOrder(order);

        logger.info(String.format("addOrder: end (pendingOrders=%d, userOrders=%d)", orderQueue.size(), u.getOrderList().size()));
    }

    @Override
    public Order deliverOrder() {
        logger.info("deliverOrder(): start");
        Order served = orderQueue.poll(); // Sacar el siguiente pedido pendiente (FIFO)
        if (served == null) {
            logger.info("deliverOrder(): no hay pedidos pendientes (end)");
            return null;
        }

        // Lista de productos vendidos en este pedido
        List<Order.Item> items = served.getProductList();

        if (items != null) {
            // Recorremos todos los productos del pedido
            for (Order.Item item : items) {
                String productId = item.getProductId();
                int quantity = item.getQuantity();
                if (quantity <= 0) {
                    throw new IllegalArgumentException("Invalid quantity for productId=" + productId);
                }

                // Buscar el producto en la lista global de productos
                for (Product p : productList) {
                    if (p.getId().equals(productId)) {
                        // Aumentar sus ventas
                        p.increaseSales(quantity);

                        logger.info("deliverOrder(): producto " + p.getName() +
                                " incrementa ventas en " + quantity +
                                " (total ventas=" + p.getSales() + ")");
                        break; // Salimos del bucle interno
                    }
                }
            }
        } else {
            logger.warn("deliverOrder(): pedido sin items");
        }
        // Añadir el pedido a la lista de servidos
        servedOrders.add(served);
        logger.info("deliverOrder(): pedido " + served.getId() +
                " añadido a la lista de pedidos servidos (total=" + servedOrders.size() + ")");

        logger.info("deliverOrder(): end (servedOrderId=" + served.getId() + ")");
        return served;
    }

    @Override
    public int numOrders() {
        logger.info("numOrders(): start");
        int size = orderQueue.size();
        logger.info("numOrders(): end = " + size);
        return size;
    }

    @Override
    public Product getProduct(String id) {
        logger.info("getProduct(id=" + id + "): start");

        for (Product p : productList) {
            if (p.getId().equals(id)) {
                logger.info("getProduct(): found product " + p.getName());
                return p;
            }
        }

        logger.warn("getProduct(): no se encontró producto con id=" + id);
        return null;
    }

    @Override
    public List<Order> getServedOrdersByUser(String userId) {
        logger.info("getServedOrdersByUser(userId=" + userId + "): start");

        List<Order> userOrders = new ArrayList<>();

        for (Order o : servedOrders) {
            if (o.getUser().getId().equals(userId)) {
                userOrders.add(o);

                // --- Mostrar información detallada del pedido ---
                StringBuilder sb = new StringBuilder();
                sb.append("Pedido ").append(o.getId()).append(" -> [");

                if (o.getProductList() != null && !o.getProductList().isEmpty()) {
                    for (Order.Item it : o.getProductList()) {
                        Product p = getProduct(it.getProductId());
                        String name = (p != null) ? p.getName() : "Producto desconocido";
                        sb.append(it.getQuantity()).append("x ").append(name).append(", ");
                    }
                    sb.setLength(sb.length() - 2); // quitar coma final
                } else {
                    sb.append("sin productos");
                }
                sb.append("]");

                logger.info(sb.toString());
            }
        }

        if (userOrders.isEmpty()) {
            logger.info("getServedOrdersByUser(): no hay pedidos servidos para el usuario " + userId);
        } else {
            logger.info("getServedOrdersByUser(): encontrados " + userOrders.size() + " pedidos servidos");
        }

        return userOrders;
    }

    @Override
    public User getUser(String id) {
        logger.info("getUser(id=" + id + "): start");
        User u = users.get(id);
        logger.info("getUser: end (found=" + (u != null) + ")");
        return u;
    }

    @Override
    public void addUser(User user) {
        logger.info("addUser(user=" + (user != null ? user.getId() : "null") + "): start");
        if (user == null || user.getId() == null) {
            logger.error("addUser: user o user.id es null");
            throw new IllegalArgumentException("user y user.id no pueden ser null");
        }
        // Si ya existe, no lo sobrescribimos; solo actualizamos el nombre si viene
        User existing = users.get(user.getId());
        if (existing == null) {
            users.put(user.getId(), user);
            logger.info("addUser: registrado userId=" + user.getId());
        } else {
            if (user.getName() != null) existing.setName(user.getName());
            logger.info("addUser: ya existía userId=" + user.getId() + " (posible actualización de nombre)");
        }
        logger.info("addUser(): end (totalUsers=" + users.size() + ")");
    }


    public User addUser(String id, String name) {
        logger.info("addUser(id=" + id + ", name=" + name + "): start");
        if (id == null) {
            logger.error("addUser: id es null");
            throw new IllegalArgumentException("id no puede ser null");
        }
        User u = users.get(id);
        if (u == null) {
            u = new User(id);
            u.setName(name);
            users.put(id, u);
            logger.info("addUser: registrado userId=" + id);
        } else {
            if (name != null) u.setName(name);
            logger.info("addUser: ya existía userId=" + id + " (posible actualización de nombre)");
        }
        logger.info("addUser(): end");
        return u;
    }



    public void clear() {
        logger.info("clear(): start");
        productList.clear();
        users.clear();
        orderQueue.clear();
        servedOrders.clear();
        logger.info("clear(): end");
    }


}