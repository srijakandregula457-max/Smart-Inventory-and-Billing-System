
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class InventoryManager {

    private ArrayList<Product> products = new ArrayList<>();
    private final String FILE_NAME = "products.txt";

    public InventoryManager() {
        loadProducts();
    }

    public void addProduct(Product product) {
        if (product == null) {
            return;
        }

        products.add(product);
        saveProducts();
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public Product searchProduct(String productId) {
        for (Product product : products) {
            if (product.getProductId().equalsIgnoreCase(productId)) {
                return product;
            }
        }
        return null;
    }

    public Product searchProductByName(String name) {
        for (Product product : products) {
            if (product.getName().equalsIgnoreCase(name)) {
                return product;
            }
        }
        return null;
    }

    public boolean deleteProduct(String productId) {
        Product product = searchProduct(productId);

        if (product != null) {
            products.remove(product);
            saveProducts();
            return true;
        }

        return false;
    }

    public boolean updateProduct(Product updatedProduct) {
        if (updatedProduct == null) {
            return false;
        }

        Product existing = searchProduct(updatedProduct.getProductId());

        if (existing == null) {
            return false;
        }

        existing.setName(updatedProduct.getName());
        existing.setCategory(updatedProduct.getCategory());
        existing.setPrice(updatedProduct.getPrice());
        existing.setQuantity(updatedProduct.getQuantity());
        existing.setExpiryDate(updatedProduct.getExpiryDate());

        saveProducts();
        return true;
    }

    // Deduct stock after a completed sale.
    public boolean updateStock(String productId, int quantitySold) {
        if (quantitySold <= 0) {
            return false;
        }

        return adjustStock(productId, -quantitySold);
    }

    // Increase or decrease stock.
    // Positive quantity = add stock.
    // Negative quantity = remove stock.
    public boolean adjustStock(String productId, int change) {

        Product product = searchProduct(productId);

        if (product == null || change == 0) {
            return false;
        }

        int newQuantity = product.getQuantity() + change;

        if (newQuantity < 0) {
            return false;
        }

        product.setQuantity(newQuantity);
        saveProducts();

        return true;
    }

    // Add returned or newly received stock.
    public boolean addStock(String productId, int quantity) {

        if (quantity <= 0) {
            return false;
        }

        return adjustStock(productId, quantity);
    }

    // Remove expired or damaged stock.
    public boolean removeStock(String productId, int quantity) {

        if (quantity <= 0) {
            return false;
        }

        return adjustStock(productId, -quantity);
    }

    public double getTotalInventoryValue() {
        double total = 0;

        for (Product product : products) {
            total += product.getPrice() * product.getQuantity();
        }

        return total;
    }

    public int getLowStockCount() {
        int count = 0;

        for (Product product : products) {
            if (product.getQuantity() <= 5) {
                count++;
            }
        }

        return count;
    }

    public int getExpiredCount() {
        int count = 0;

        for (Product product : products) {
            if ("EXPIRED".equals(product.getExpiryStatus())) {
                count++;
            }
        }

        return count;
    }

    public void saveProducts() {
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(FILE_NAME))) {

            for (Product product : products) {

                String expiry = product.getExpiryDate() == null
                        ? ""
                        : product.getExpiryDate().toString();

                writer.write(
                        product.getProductId() + "," +
                        product.getName() + "," +
                        product.getCategory() + "," +
                        product.getPrice() + "," +
                        product.getQuantity() + "," +
                        expiry
                );

                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println(
                    "Error saving products: " + e.getMessage()
            );
        }
    }

    public void loadProducts() {
        products.clear();

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                try {
                    String[] data = line.split(",", -1);

                    if (data.length < 5) {
                        continue;
                    }

                    String id = data[0];
                    String name = data[1];
                    String category = data[2];

                    double price = Double.parseDouble(data[3]);
                    int quantity = Integer.parseInt(data[4]);

                    LocalDate expiryDate = null;

                    if (data.length >= 6 && !data[5].isBlank()) {
                        expiryDate = LocalDate.parse(data[5]);
                    }

                    Product product = new Product(
                            id, name, category,
                            price, quantity, expiryDate
                    );

                    products.add(product);

                } catch (NumberFormatException
                         | DateTimeParseException e) {

                    System.out.println(
                            "Skipping invalid product record: " + line
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Error loading products: " + e.getMessage()
            );
        }
    }
}
