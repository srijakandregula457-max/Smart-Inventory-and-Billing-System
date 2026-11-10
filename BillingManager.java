
public class BillingManager {

    private static final double DISCOUNT_RATE = 0.10;
    private static final double MINIMUM_DISCOUNT_AMOUNT = 1000.0;

    public double calculateBill(Product product, int quantity) {

        if (product == null || quantity <= 0) {
            return 0;
        }

        if (quantity > product.getQuantity()) {
            return 0;
        }

        if (product.getExpiryDate() == null ||
                product.getExpiryDate().isBefore(
                        java.time.LocalDate.now())) {
            return 0;
        }

        double subtotal = product.getPrice() * quantity;

        if (subtotal >= MINIMUM_DISCOUNT_AMOUNT) {
            return subtotal - (subtotal * DISCOUNT_RATE);
        }

        return subtotal;
    }

    public double calculateDiscount(double subtotal) {

        if (subtotal >= MINIMUM_DISCOUNT_AMOUNT) {
            return subtotal * DISCOUNT_RATE;
        }

        return 0;
    }

    public void displayBill(Product product, int quantity) {

        double subtotal = product.getPrice() * quantity;
        double discount = calculateDiscount(subtotal);
        double total = subtotal - discount;

        System.out.println("========== CUSTOMER BILL ==========");
        System.out.println("Product: " + product.getName());
        System.out.println("Quantity: " + quantity);
        System.out.println("Price: Rs. " + product.getPrice());
        System.out.println("Subtotal: Rs. " + subtotal);
        System.out.println("Discount: Rs. " + discount);
        System.out.println("Final Amount: Rs. " + total);
        System.out.println("===================================");
    }
}
