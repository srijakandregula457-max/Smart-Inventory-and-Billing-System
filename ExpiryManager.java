
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ExpiryManager {

    private static final String REMOVED_FILE = "removed_stock.txt";
    private static final String RETURNED_FILE = "returned_stock.txt";

    // Find expired and soon-to-expire products.
    public List<String> getExpiryAlerts(List<Product> products) {

        List<String> alerts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Product product : products) {

            LocalDate expiry = product.getExpiryDate();

            if (expiry == null) {
                continue;
            }

            long daysLeft = ChronoUnit.DAYS.between(today, expiry);

            if (daysLeft < 0) {
                alerts.add(product.getName()
                        + " - EXPIRED "
                        + Math.abs(daysLeft) + " days ago"
                        + " | Quantity: " + product.getQuantity());

            } else if (daysLeft <= 30) {
                alerts.add(product.getName()
                        + " - Expires in " + daysLeft + " days"
                        + " | Quantity: " + product.getQuantity());
            }
        }

        return alerts;
    }

    // Suggest discount based on remaining shelf life.
    public int getSuggestedDiscount(Product product) {

        if (product == null || product.getExpiryDate() == null) {
            return 0;
        }

        long daysLeft = ChronoUnit.DAYS.between(
                LocalDate.now(), product.getExpiryDate());

        if (daysLeft < 0) {
            return 0;
        } else if (daysLeft <= 7) {
            return 30;
        } else if (daysLeft <= 14) {
            return 20;
        } else if (daysLeft <= 30) {
            return 10;
        }

        return 0;
    }

    // Calculate the suggested price after discount.
    public double getDiscountedPrice(Product product) {

        int discount = getSuggestedDiscount(product);

        return product.getPrice() * (100 - discount) / 100.0;
    }

    // Record removed stock and update inventory.
    // Use this for expired, damaged or unsellable products.
    public boolean recordRemovedStock(
            InventoryManager inventory,
            Product product,
            int quantity,
            String reason) {

        if (inventory == null || product == null
                || quantity <= 0
                || reason == null || reason.isBlank()) {
            return false;
        }

        Product current = inventory.searchProduct(product.getProductId());

        if (current == null || current.getQuantity() < quantity) {
            return false;
        }

        double loss = current.getPrice() * quantity;

        String record = LocalDate.now()
                + " | ID: " + current.getProductId()
                + " | Product: " + current.getName()
                + " | Quantity: " + quantity
                + " | Reason: " + reason
                + " | Potential Loss: Rs. "
                + String.format("%.2f", loss);

        if (!inventory.removeStock(current.getProductId(), quantity)) {
            return false;
        }

        appendRecord(REMOVED_FILE, record);
        return true;
    }

    // Record a customer return.
    // This version assumes the returned items were inspected
    // and are suitable to be added back to sellable stock.
    public boolean recordReturnedStock(
            InventoryManager inventory,
            Product product,
            int quantity,
            String reason) {

        if (inventory == null || product == null
                || quantity <= 0
                || reason == null || reason.isBlank()) {
            return false;
        }

        Product current = inventory.searchProduct(product.getProductId());

        if (current == null) {
            return false;
        }

        double value = current.getPrice() * quantity;

        String record = LocalDate.now()
                + " | ID: " + current.getProductId()
                + " | Product: " + current.getName()
                + " | Quantity: " + quantity
                + " | Reason: " + reason
                + " | Returned Value: Rs. "
                + String.format("%.2f", value);

        if (!inventory.addStock(current.getProductId(), quantity)) {
            return false;
        }

        appendRecord(RETURNED_FILE, record);
        return true;
    }

    // Save a record to a text file.
    private void appendRecord(String fileName, String record) {

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(fileName, true))) {

            writer.write(record);
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not save stock record: " + e.getMessage(), e);
        }
    }

    // Read removed-stock records.
    public String getRemovedStockReport() {
        return readRecords(REMOVED_FILE);
    }

    // Read returned-stock records.
    public String getReturnedStockReport() {
        return readRecords(RETURNED_FILE);
    }

    // Calculate total potential loss from removed stock.
    public double getTotalPotentialLoss() {

        double totalLoss = 0;
        File file = new File(REMOVED_FILE);

        if (!file.exists()) {
            return 0;
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;
            String marker = "Potential Loss: Rs.";

            while ((line = reader.readLine()) != null) {

                int index = line.indexOf(marker);

                if (index >= 0) {
                    String amount = line.substring(
                            index + marker.length()).trim();

                    totalLoss += Double.parseDouble(amount);
                }
            }

        } catch (IOException | NumberFormatException e) {
            System.out.println(
                    "Error calculating potential loss: " + e.getMessage());
        }

        return totalLoss;
    }

    // Read records from a text file.
    private String readRecords(String fileName) {

        File file = new File(fileName);

        if (!file.exists()) {
            return "No records available.";
        }

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

        } catch (IOException e) {
            return "Error reading records: " + e.getMessage();
        }

        if (result.length() == 0) {
            return "No records available.";
        }

        return result.toString();
    }
}
