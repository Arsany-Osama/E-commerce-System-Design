import java.util.*;

// Interface for any product that needs shipping
interface Shippable {
    String getName();
    double getWeight();
}

// Abstract base class for all products
abstract class Product {
    protected String name;
    protected double price;
    protected int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void reduceQuantity(int qty) {
        this.quantity -= qty;
    }

    // Override this in expirable products
    public boolean isExpired() {
        return false;
    }

    // Checks if the product needs shipping
    public boolean isShippable() {
        return this instanceof Shippable;
    }

    public String toString() {
        return name + " (" + quantity + " left)";
    }
}

// ===================== Product Types ===================== //

// Cheese: needs shipping, can expire
class Cheese extends Product implements Shippable {
    private boolean expired;
    private double weight;

    public Cheese(String name, double price, int quantity, double weight, boolean expired) {
        super(name, price, quantity);
        this.weight = weight;
        this.expired = expired;
    }

    @Override
    public boolean isExpired() { return expired; }

    @Override
    public double getWeight() { return weight; }

    @Override
    public String getName() { return name; }
}

// Biscuits: same as cheese (shippable + expirable)
class Biscuits extends Product implements Shippable {
    private boolean expired;
    private double weight;

    public Biscuits(String name, double price, int quantity, double weight, boolean expired) {
        super(name, price, quantity);
        this.weight = weight;
        this.expired = expired;
    }

    @Override
    public boolean isExpired() { return expired; }

    @Override
    public double getWeight() { return weight; }

    @Override
    public String getName() { return name; }
}

// TV: needs shipping, never expires
class TV extends Product implements Shippable {
    private double weight;

    public TV(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    @Override
    public double getWeight() { return weight; }

    @Override
    public String getName() { return name; }
}

// ScratchCard: doesn't need shipping, doesn't expire
class ScratchCard extends Product {
    public ScratchCard(String name, double price, int quantity) {
        super(name, price, quantity);
    }
}

// ===================== Cart Item ===================== //

class CartItem {
    Product product;
    int quantity;

    public CartItem(Product p, int qty) {
        this.product = p;
        this.quantity = qty;
    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}

// ===================== Customer ===================== //

class Customer {
    private String name;
    private double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public double getBalance() { return balance; }

    public void deduct(double amount) {
        this.balance -= amount;
    }

    public String getName() { return name; }
}

// ===================== Cart ===================== //

class Cart {
    List<CartItem> items = new ArrayList<>();

    public void add(Product product, int qty) {
        if (qty > product.getQuantity()) {
            System.out.println("Cannot add more than available stock for " + product.getName());
            return;
        }
        items.add(new CartItem(product, qty));
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<CartItem> getItems() {
        return items;
    }
}

// ===================== Shipping Service ===================== //

class ShippingService {
    public static void ship(List<Shippable> items) {
        System.out.println("** Shipment notice **");

        double totalWeight = 0;
        Map<String, Integer> itemCount = new LinkedHashMap<>();
        Map<String, Double> weightMap = new HashMap<>();

        // Count how many of each item and total their weights
        for (Shippable item : items) {
            itemCount.put(item.getName(), itemCount.getOrDefault(item.getName(), 0) + 1);
            weightMap.put(item.getName(), item.getWeight());
            totalWeight += item.getWeight();
        }

        // Print each item shipping info
        for (String name : itemCount.keySet()) {
            int count = itemCount.get(name);
            double weight = weightMap.get(name) * count;
            System.out.printf("%dx %s %.0fg%n", count, name, weight);
        }

        System.out.printf("Total package weight %.1fkg%n", totalWeight / 1000.0);
    }
}

// ===================== Checkout Logic ===================== //

class CheckoutService {
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Error: Cart is empty.");
            return;
        }

        double subtotal = 0;
        List<Shippable> toShip = new ArrayList<>();

        // Loop through cart to validate everything
        for (CartItem item : cart.getItems()) {
            Product product = item.product;
            int qty = item.quantity;

            if (product.isExpired()) {
                System.out.println("Error: Product " + product.getName() + " is expired.");
                return;
            }

            if (qty > product.getQuantity()) {
                System.out.println("Error: Not enough quantity for " + product.getName());
                return;
            }

            // If it needs shipping, add it to the shipping list
            if (product instanceof Shippable) {
                for (int i = 0; i < qty; i++) {
                    toShip.add((Shippable) product);
                }
            }

            subtotal += product.getPrice() * qty;
        }

        double shipping = toShip.isEmpty() ? 0 : 30;  // Flat rate shipping
        double total = subtotal + shipping;

        if (customer.getBalance() < total) {
            System.out.println("Error: Insufficient balance.");
            return;
        }

        // Call shipping service if needed
        if (!toShip.isEmpty()) {
            ShippingService.ship(toShip);
        }

        // Deduct payment
        customer.deduct(total);

        // Print receipt
        System.out.println(">> Checkout receipt <<");
        for (CartItem item : cart.getItems()) {
            System.out.printf("%dx %s %.0f%n", item.quantity, item.product.getName(), item.getTotalPrice());
            item.product.reduceQuantity(item.quantity);
        }

        System.out.println("----------------------");
        System.out.printf("Subtotal %.0f%n", subtotal);
        System.out.printf("Shipping %.0f%n", shipping);
        System.out.printf("Amount %.0f%n", total);
        System.out.printf("Customer balance %.0f%n", customer.getBalance());
    }
}

// ===================== Main App Entry ===================== //

public class main {
    public static void main(String[] args) {
        // Create products
        Product cheese = new Cheese("Cheese", 100, 5, 200, false);     // 200g
        Product biscuits = new Biscuits("Biscuits", 150, 3, 700, false); // 700g
        Product tv = new TV("TV", 1000, 2, 10000);                      // 10kg
        Product scratchCard = new ScratchCard("ScratchCard", 50, 10);  // digital

        // Create customer and cart
        Customer customer = new Customer("Ahmed", 1000);
        Cart cart = new Cart();

        // Add items to cart
        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(scratchCard, 1);

        // Checkout
        CheckoutService.checkout(customer, cart);
    }
}
/*
Console Output:

** Shipment notice **
2x Cheese 400g
1x Biscuits 700g
Total package weight 1.1kg
>> Checkout receipt <<
2x Cheese 200
1x Biscuits 150
1x ScratchCard 50
----------------------
Subtotal 400
Shipping 30
Amount 430
Customer balance 570
PS C:\Users\Win10\Desktop\E-commerce> 
*/
