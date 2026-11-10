
import java.time.LocalDate;

public class Product {
    private String productId;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private LocalDate expiryDate;

    public Product(String productId, String name, String category,
                   double price, int quantity, LocalDate expiryDate) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }

    public Product(String productId, String name, String category,
                   double price, int quantity) {
        this(productId, name, category, price, quantity, null);
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getExpiryStatus() {
        if (expiryDate == null) {
            return "Date not set";
        }

        LocalDate today = LocalDate.now();

        if (expiryDate.isBefore(today)) {
            return "EXPIRED";
        }

        if (!expiryDate.isAfter(today.plusDays(30))) {
            return "Expiring soon";
        }

        return "Valid";
    }
}
