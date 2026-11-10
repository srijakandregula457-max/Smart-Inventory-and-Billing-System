
public class CartItem {

    private Product product;
    private int quantity;

    // Constructor
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Get product
    public Product getProduct() {
        return product;
    }

    // Get quantity
    public int getQuantity() {
        return quantity;
    }

    // Change quantity
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Calculate subtotal
    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    // Get product ID
    public String getProductId() {
        return product.getProductId();
    }

    // Get product name
    public String getProductName() {
        return product.getName();
    }

    // Get product price
    public double getPrice() {
        return product.getPrice();
    }
}
