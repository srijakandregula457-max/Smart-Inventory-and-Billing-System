
import java.util.ArrayList;

public class CartManager {

    private ArrayList<CartItem> cartItems = new ArrayList<>();

    // Get all cart items
    public ArrayList<CartItem> getCartItems() {
        return cartItems;
    }

    // Add product to cart
    public boolean addToCart(Product product, int quantity) {

        if (product == null || quantity <= 0) {
            return false;
        }

        if (product.getExpiryDate() == null ||
                product.getExpiryDate().isBefore(
                        java.time.LocalDate.now())) {
            return false;
        }

        for (CartItem item : cartItems) {

            if (item.getProductId().equalsIgnoreCase(
                    product.getProductId())) {

                int newQuantity = item.getQuantity() + quantity;

                if (newQuantity > product.getQuantity()) {
                    return false;
                }

                item.setQuantity(newQuantity);
                return true;
            }
        }

        if (quantity > product.getQuantity()) {
            return false;
        }

        cartItems.add(new CartItem(product, quantity));
        return true;
    }

    // Remove product from cart
    public boolean removeFromCart(String productId) {

        return cartItems.removeIf(
                item -> item.getProductId().equalsIgnoreCase(productId)
        );
    }

    // Clear cart
    public void clearCart() {
        cartItems.clear();
    }

    // Calculate subtotal
    public double calculateSubtotal() {

        double total = 0;

        for (CartItem item : cartItems) {
            total += item.getSubtotal();
        }

        return total;
    }

    // Calculate 10% discount for bills of Rs. 1000 or more
    public double calculateDiscount() {

        double subtotal = calculateSubtotal();

        if (subtotal >= 1000) {
            return subtotal * 0.10;
        }

        return 0;
    }

    // Calculate final amount
    public double calculateTotal() {
        return calculateSubtotal() - calculateDiscount();
    }

    // Check whether cart is empty
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    // Check stock before checkout
    public boolean checkStock() {

        for (CartItem item : cartItems) {

            Product product = item.getProduct();

            if (product.getQuantity() < item.getQuantity()) {
                return false;
            }

            if (product.getExpiryDate() == null ||
                    product.getExpiryDate().isBefore(
                            java.time.LocalDate.now())) {
                return false;
            }
        }

        return true;
    }
}
