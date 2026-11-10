
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class SalesManager {

    private static final String FILE_NAME = "sales.txt";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public void recordSale(
            String productName,
            int quantity,
            double amount) {

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(FILE_NAME, true))) {

            String record = LocalDateTime.now().format(DATE_TIME_FORMAT)
                    + " | " + productName
                    + " | Quantity: " + quantity
                    + " | Amount: Rs. "
                    + String.format("%.2f", amount);

            writer.write(record);
            writer.newLine();

        } catch (IOException e) {
            System.out.println("Error recording sale: " + e.getMessage());
        }
    }

    public String getSalesReport() {

        StringBuilder report = new StringBuilder();

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return "No sales recorded yet.";
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {
                report.append(line).append("\n");
            }

        } catch (IOException e) {
            return "Error reading sales: " + e.getMessage();
        }

        if (report.length() == 0) {
            return "No sales recorded yet.";
        }

        return report.toString();
    }

    public Map<LocalDate, Double> getDailySalesTotals() {

        Map<LocalDate, Double> dailyTotals = new TreeMap<>();

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return dailyTotals;
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                try {
                    String[] parts = line.split("\\|");

                    if (parts.length < 4) {
                        continue;
                    }

                    String dateTimeText = parts[0].trim();

                    LocalDate saleDate = LocalDate.parse(
                            dateTimeText.substring(0, 10),
                            DATE_FORMAT
                    );

                    String amountText = parts[3]
                            .replace("Amount:", "")
                            .replace("Rs.", "")
                            .trim();

                    double amount = Double.parseDouble(amountText);

                    dailyTotals.put(
                            saleDate,
                            dailyTotals.getOrDefault(saleDate, 0.0)
                                    + amount
                    );

                } catch (Exception ignored) {
                    // Skip records with an unexpected format.
                }
            }

        } catch (IOException e) {
            System.out.println(
                    "Error calculating daily sales: " + e.getMessage());
        }

        return dailyTotals;
    }

    public double getTotalRevenue() {

        double total = 0;

        for (double dailyAmount : getDailySalesTotals().values()) {
            total += dailyAmount;
        }

        return total;
    }
}
