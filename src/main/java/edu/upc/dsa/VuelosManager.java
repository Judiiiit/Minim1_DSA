package edu.upc.dsa;

import edu.upc.dsa.models.Product;
import edu.upc.dsa.models.Order;
import edu.upc.dsa.models.User;

import java.util.List;

public interface ProductManager {
    public void addProduct(String id, String name, double price);
    public List<Product> getProductsByPrice();
    public List<Product> getProductsBySales();
    public void addOrder(Order order);
    public Order deliverOrder();
    public int numOrders();
    List<Order> getServedOrdersByUser(String userId);
    Product getProduct(String id);
    User getUser(String number);

    // PARA LA RESTAPI
    public int size();
    public List<Product> findAll();
    public void addUser (User user);
    public User addUser(String Id, String name);


}
