import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Main extends Application {

    private final InventoryManager inventory = new InventoryManager();
    private final CartManager cart = new CartManager();
    private final SalesManager sales = new SalesManager();
    private final ExpiryManager expiryManager = new ExpiryManager();

    private final ObservableList<Product> products =
            FXCollections.observableArrayList();

    private final ObservableList<CartItem> cartItems =
            FXCollections.observableArrayList();

    private final TableView<Product> inventoryTable = new TableView<>();
    private final TableView<CartItem> cartTable = new TableView<>();

    private final ComboBox<Product> productChoice = new ComboBox<>();
    private final TextField cartQuantity = new TextField();

    private final Label subtotalLabel = new Label("Rs. 0.00");
    private final Label discountLabel = new Label("Rs. 0.00");
    private final Label totalLabel = new Label("Rs. 0.00");

    private final Label totalProducts = new Label("0");
    private final Label lowStock = new Label("0");
    private final Label expiredCount = new Label("0");
    private final Label inventoryValue = new Label("Rs. 0.00");

    private final String BG = "#0B1220";
    private final String PANEL = "#151F32";
    private final String BLUE = "#4F8CFF";
    private final String TEXT = "#F4F7FB";

    private BorderPane root;
    private VBox content;

    @Override
    public void start(Stage stage) {

        root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");

        VBox sidebar = createSidebar();

        content = new VBox(18);
        content.setPadding(new Insets(25));

        root.setLeft(sidebar);
        root.setCenter(content);

        showDashboard();

        Scene scene = new Scene(root, 1250, 850);

        stage.setTitle("Smart Inventory and Billing Management System");
        stage.setMinWidth(1050);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    // SIDEBAR AND NAVIGATION

    private VBox createSidebar() {

        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setPrefWidth(215);
        sidebar.setStyle("-fx-background-color: #101A2B;");

        Label logo = new Label("✦ SMART STOCK");
        logo.setStyle(
                "-fx-text-fill: #75A7FF;" +
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle = new Label("INVENTORY MANAGEMENT");
        subtitle.setStyle("-fx-text-fill: #8FA2BD;");

        Button dashboard = menuButton("Dashboard");
        Button inventoryButton = menuButton("Inventory");
        Button billingButton = menuButton("Billing");
        Button expiryButton = menuButton("Expiry Report");
        Button salesButton = menuButton("Sales Report");
        Button analyticsButton = menuButton("Analytics & Expiry Management");

        dashboard.setOnAction(e -> showDashboard());
        inventoryButton.setOnAction(e -> showInventory());
        billingButton.setOnAction(e -> showBilling());
        expiryButton.setOnAction(e -> showExpiryReport());
        salesButton.setOnAction(e -> showSalesReport());
        analyticsButton.setOnAction(e -> showAnalyticsAndExpiry());

        sidebar.getChildren().addAll(
                logo,
                subtitle,
                new Separator(),
                dashboard,
                inventoryButton,
                billingButton,
                expiryButton,
                salesButton,
                analyticsButton
        );

        return sidebar;
    }

    private Button menuButton(String name) {

        Button button = new Button(name);

        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);

        button.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #D0DCEE;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 12;"
        );

        return button;
    }

    private void setPage(String title, String subtitle) {

        content.getChildren().clear();

        Label heading = new Label(title);
        heading.setStyle(
                "-fx-text-fill: " + TEXT +
                "; -fx-font-size: 28px; -fx-font-weight: bold;"
        );

        Label description = new Label(subtitle);
        description.setStyle("-fx-text-fill: #9AAAC0;");

        content.getChildren().addAll(heading, description);
    }

    // DASHBOARD

    private void showDashboard() {

        setPage(
                "Dashboard Overview",
                "Monitor your products, stock and business activity."
        );

        updateStatistics();

        HBox cards = new HBox(12);

        cards.getChildren().addAll(
                createCard("TOTAL PRODUCTS", totalProducts, "#75A7FF"),
                createCard("LOW STOCK", lowStock, "#F6C76B"),
                createCard("EXPIRED ITEMS", expiredCount, "#FF7D87"),
                createCard("STOCK VALUE", inventoryValue, "#63D6B0")
        );

        VBox information = panelBox();

        Label title = sectionTitle("Welcome to Smart Stock");

        Label message = new Label(
                "Manage inventory, track expiry dates, " +
                "generate customer bills and monitor sales."
        );

        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #C2CEE0;");

        Button inventoryButton = actionButton("Open Inventory");
        Button billingButton = actionButton("Start Billing");

        inventoryButton.setOnAction(e -> showInventory());
        billingButton.setOnAction(e -> showBilling());

        information.getChildren().addAll(
                title,
                message,
                inventoryButton,
                billingButton
        );

        content.getChildren().addAll(cards, information);
    }

    private VBox createCard(String title, Label value, String color) {

        VBox card = new VBox(10);

        card.setPadding(new Insets(18));
        card.setPrefWidth(210);

        card.setStyle(
                "-fx-background-color: " + PANEL +
                "; -fx-background-radius: 12;"
        );

        Label heading = new Label(title);
        heading.setStyle("-fx-text-fill: #9AAAC0;");

        value.setStyle(
                "-fx-text-fill: " + color +
                "; -fx-font-size: 22px; -fx-font-weight: bold;"
        );

        card.getChildren().addAll(heading, value);

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    private void updateStatistics() {

        int total = inventory.getProducts().size();
        int low = 0;
        int expired = 0;
        double value = 0;

        for (Product p : inventory.getProducts()) {

            if (p.getQuantity() <= 5) {
                low++;
            }

            if ("EXPIRED".equals(p.getExpiryStatus())) {
                expired++;
            }

            value += p.getPrice() * p.getQuantity();
        }

        totalProducts.setText(String.valueOf(total));
        lowStock.setText(String.valueOf(low));
        expiredCount.setText(String.valueOf(expired));

        inventoryValue.setText(
                "Rs. " + String.format("%.2f", value)
        );
    }

    // INVENTORY PAGE

    private void showInventory() {

        setPage(
                "Product Inventory",
                "Add, search, edit and delete your products."
        );

        Button add = actionButton("Add Product");
        Button edit = actionButton("Edit Selected");
        Button delete = dangerButton("Delete Selected");
        Button refresh = secondaryButton("Refresh");

        TextField search = new TextField();
        search.setPromptText("Search by ID or name");
        search.setPrefWidth(220);
        search.setStyle(fieldStyle());

        add.setOnAction(e -> addProductDialog());
        delete.setOnAction(e -> deleteSelectedProduct());
        edit.setOnAction(e -> editSelectedProduct());
        refresh.setOnAction(e -> refreshInventory());

        Button searchButton = actionButton("Search");

        searchButton.setOnAction(e -> {

            String query = search.getText().trim().toLowerCase();

            products.clear();

            for (Product p : inventory.getProducts()) {

                if (p.getProductId().toLowerCase().contains(query)
                        || p.getName().toLowerCase().contains(query)) {
                    products.add(p);
                }
            }
        });

        HBox actions = new HBox(10, add, edit, delete, refresh);
        HBox searchBar = new HBox(10, search, searchButton);

        createInventoryTable();
        refreshInventory();

        VBox panel = panelBox();

        panel.getChildren().addAll(
                actions,
                searchBar,
                inventoryTable
        );

        content.getChildren().add(panel);
    }

    private void createInventoryTable() {

        inventoryTable.getColumns().clear();

        TableColumn<Product, String> id =
                new TableColumn<>("Product ID");

        id.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getProductId()
                )
        );

        TableColumn<Product, String> name =
                new TableColumn<>("Name");

        name.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getName()
                )
        );

        TableColumn<Product, String> category =
                new TableColumn<>("Category");

        category.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getCategory()
                )
        );

        TableColumn<Product, Number> price =
                new TableColumn<>("Price");

        price.setCellValueFactory(d ->
                new javafx.beans.property.SimpleDoubleProperty(
                        d.getValue().getPrice()
                )
        );

        TableColumn<Product, Number> quantity =
                new TableColumn<>("Quantity");

        quantity.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(
                        d.getValue().getQuantity()
                )
        );

        TableColumn<Product, String> expiry =
                new TableColumn<>("Expiry Date");

        expiry.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getExpiryDate() == null
                                ? "Not set"
                                : d.getValue().getExpiryDate().toString()
                )
        );

        TableColumn<Product, String> expiryStatus =
                new TableColumn<>("Expiry Status");

        expiryStatus.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getExpiryStatus()
                )
        );

        inventoryTable.getColumns().addAll(
                id, name, category, price, quantity,
                expiry, expiryStatus
        );

        inventoryTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        inventoryTable.setPrefHeight(400);
        inventoryTable.setItems(products);

        inventoryTable.setRowFactory(tv -> new TableRow<>() {

            @Override
            protected void updateItem(Product item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                } else if ("EXPIRED".equals(item.getExpiryStatus())) {
                    setStyle("-fx-background-color: #512B37;");
                } else if ("Expiring soon".equals(item.getExpiryStatus())) {
                    setStyle("-fx-background-color: #51452B;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void refreshInventory() {

        products.setAll(inventory.getProducts());
        updateStatistics();
        refreshProductChoices();
    }

    private void addProductDialog() {

        Dialog<Product> dialog = new Dialog<>();

        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Enter product information");

        ButtonType saveButton = new ButtonType(
                "Save Product",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton, ButtonType.CANCEL
        );

        TextField id = new TextField();
        TextField name = new TextField();
        TextField category = new TextField();
        TextField price = new TextField();
        TextField quantity = new TextField();

        DatePicker expiry = new DatePicker();

        id.setPromptText("Product ID");
        name.setPromptText("Product name");
        category.setPromptText("Category");
        price.setPromptText("Price");
        quantity.setPromptText("Quantity");
        expiry.setPromptText("Expiry date");

        VBox fields = new VBox(
                10, id, name, category, price, quantity, expiry
        );

        dialog.getDialogPane().setContent(fields);

        dialog.setResultConverter(button -> {

            if (button == saveButton) {

                try {

                    if (id.getText().isBlank()
                            || name.getText().isBlank()
                            || category.getText().isBlank()
                            || expiry.getValue() == null) {
                        showMessage("Please enter all product details.");
                        return null;
                    }

                    double p = Double.parseDouble(price.getText());
                    int q = Integer.parseInt(quantity.getText());

                    if (p <= 0 || q < 0) {
                        showMessage(
                                "Price must be positive and quantity cannot be negative."
                        );
                        return null;
                    }

                    if (inventory.searchProduct(id.getText().trim()) != null) {
                        showMessage("Product ID already exists.");
                        return null;
                    }

                    return new Product(
                            id.getText().trim(),
                            name.getText().trim(),
                            category.getText().trim(),
                            p,
                            q,
                            expiry.getValue()
                    );

                } catch (NumberFormatException ex) {
                    showMessage("Enter a valid price and quantity.");
                    return null;
                }
            }

            return null;
        });

        dialog.showAndWait().ifPresent(product -> {

            inventory.addProduct(product);
            refreshInventory();
            showMessage("Product added successfully!");
        });
    }

    private void editSelectedProduct() {

        Product selected =
                inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Select a product to edit.");
            return;
        }

        TextInputDialog nameDialog =
                new TextInputDialog(selected.getName());

        nameDialog.setTitle("Edit Product");
        nameDialog.setHeaderText("Update product name");
        nameDialog.setContentText("Product name:");

        nameDialog.showAndWait().ifPresent(newName -> {

            if (newName.isBlank()) {
                showMessage("Product name cannot be empty.");
                return;
            }

            selected.setName(newName.trim());
            inventory.saveProducts();
            refreshInventory();

            showMessage("Product updated successfully!");
        });
    }

    private void deleteSelectedProduct() {

        Product selected =
                inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Please select a product first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Delete Product");
        confirm.setHeaderText("Delete " + selected.getName() + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                inventory.deleteProduct(selected.getProductId());
                refreshInventory();

                showMessage("Product deleted.");
            }
        });
    }
	    // ==========================================
    // BILLING PAGE
    // ==========================================

    private void showBilling() {

        setPage(
                "Customer Billing",
                "Add products to the cart and generate customer bills."
        );

        refreshProductChoices();

        productChoice.setPromptText("Select a product");
        productChoice.setPrefWidth(250);
        productChoice.setStyle(fieldStyle());

        productChoice.setCellFactory(list -> new ListCell<>() {

            @Override
            protected void updateItem(Product p, boolean empty) {

                super.updateItem(p, empty);

                if (empty || p == null) {
                    setText(null);
                } else {
                    setText(
                            p.getName() + " | Rs. " + p.getPrice()
                                    + " | Stock: " + p.getQuantity()
                    );
                }
            }
        });

        productChoice.setButtonCell(new ListCell<>() {

            @Override
            protected void updateItem(Product p, boolean empty) {

                super.updateItem(p, empty);

                setText(empty || p == null
                        ? "Select a product"
                        : p.getName());
            }
        });

        cartQuantity.setPromptText("Quantity");
        cartQuantity.setPrefWidth(100);
        cartQuantity.setStyle(fieldStyle());

        Button addButton = actionButton("Add to Cart");
        addButton.setOnAction(e -> addCartItem());

        HBox selection = new HBox(
                10, productChoice, cartQuantity, addButton
        );

        selection.setAlignment(Pos.CENTER_LEFT);

        createCartTable();

        Button removeButton = dangerButton("Remove Selected");
        Button clearButton = secondaryButton("Clear Cart");
        Button totalButton = secondaryButton("Calculate Total");
        Button generateButton = actionButton("Generate Bill");

        removeButton.setOnAction(e -> removeCartItem());
        clearButton.setOnAction(e -> clearCart());
        totalButton.setOnAction(e -> updateCartTotals());
        generateButton.setOnAction(e -> generateFinalBill());

        HBox cartActions = new HBox(
                10, removeButton, clearButton,
                totalButton, generateButton
        );

        VBox totals = panelBox();

        totals.getChildren().addAll(
                totalLine("Subtotal:", subtotalLabel),
                totalLine("Discount:", discountLabel),
                totalLine("Final Amount:", totalLabel)
        );

        VBox panel = panelBox();

        panel.getChildren().addAll(
                sectionTitle("Select Products"),
                selection,
                sectionTitle("Shopping Cart"),
                cartTable,
                cartActions,
                totals
        );

        content.getChildren().add(panel);

        updateCartTotals();
    }

    private void refreshProductChoices() {

        productChoice.setItems(
                FXCollections.observableArrayList(
                        inventory.getProducts().stream()
                                .filter(p -> p.getExpiryDate() != null)
                                .filter(p -> !p.getExpiryDate()
                                        .isBefore(LocalDate.now()))
                                .filter(p -> p.getQuantity() > 0)
                                .toList()
                )
        );
    }

    private void createCartTable() {

        cartTable.getColumns().clear();

        TableColumn<CartItem, String> name =
                new TableColumn<>("Product");

        name.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getProductName()
                )
        );

        TableColumn<CartItem, Number> price =
                new TableColumn<>("Price");

        price.setCellValueFactory(d ->
                new javafx.beans.property.SimpleDoubleProperty(
                        d.getValue().getPrice()
                )
        );

        TableColumn<CartItem, Number> quantity =
                new TableColumn<>("Quantity");

        quantity.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(
                        d.getValue().getQuantity()
                )
        );

        TableColumn<CartItem, Number> subtotal =
                new TableColumn<>("Subtotal");

        subtotal.setCellValueFactory(d ->
                new javafx.beans.property.SimpleDoubleProperty(
                        d.getValue().getSubtotal()
                )
        );

        cartTable.getColumns().addAll(
                name, price, quantity, subtotal
        );

        cartTable.setItems(cartItems);

        cartTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        cartTable.setPrefHeight(300);
    }

    private void addCartItem() {

        Product selected = productChoice.getValue();

        if (selected == null) {
            showMessage("Please select a product.");
            return;
        }

        try {

            int quantity = Integer.parseInt(
                    cartQuantity.getText().trim()
            );

            boolean added = cart.addToCart(selected, quantity);

            if (!added) {
                showMessage(
                        "Cannot add product. Check quantity, stock and expiry date."
                );
                return;
            }

            refreshCart();
            cartQuantity.clear();

            showMessage("Product added to cart!");

        } catch (NumberFormatException e) {
            showMessage("Enter a valid quantity.");
        }
    }

    private void removeCartItem() {

        CartItem selected =
                cartTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Select a cart item to remove.");
            return;
        }

        cart.removeFromCart(selected.getProductId());

        refreshCart();
    }

    private void clearCart() {

        if (cart.isEmpty()) {
            showMessage("Cart is already empty.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Clear Cart");
        confirm.setHeaderText("Remove all items?");
        confirm.setContentText("This will clear the current cart.");

        confirm.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                cart.clearCart();
                refreshCart();
            }
        });
    }

    private void refreshCart() {

        cartItems.setAll(cart.getCartItems());
        updateCartTotals();
    }

    private HBox totalLine(String title, Label amount) {

        Label name = new Label(title);

        name.setStyle(
                "-fx-text-fill: #C2CEE0; -fx-font-size: 14px;"
        );

        amount.setStyle(
                "-fx-text-fill: #63D6B0; " +
                "-fx-font-size: 16px; -fx-font-weight: bold;"
        );

        HBox line = new HBox(15, name, amount);

        line.setAlignment(Pos.CENTER_LEFT);

        return line;
    }

    private void updateCartTotals() {

        double subtotal = cart.calculateSubtotal();
        double discount = cart.calculateDiscount();
        double total = cart.calculateTotal();

        subtotalLabel.setText(
                "Rs. " + String.format("%.2f", subtotal)
        );

        discountLabel.setText(
                "Rs. " + String.format("%.2f", discount)
        );

        totalLabel.setText(
                "Rs. " + String.format("%.2f", total)
        );
    }

    private void generateFinalBill() {

        if (cart.isEmpty()) {
            showMessage("Your cart is empty.");
            return;
        }

        if (!cart.checkStock()) {
            showMessage(
                    "Stock or expiry validation failed. Please check the cart."
            );
            return;
        }

        StringBuilder bill = new StringBuilder();

        bill.append("SMART STOCK - CUSTOMER BILL\n");
        bill.append("--------------------------------\n");

        double subtotal = cart.calculateSubtotal();
        double discount = cart.calculateDiscount();
        double total = cart.calculateTotal();

        for (CartItem item : cart.getCartItems()) {

            bill.append(item.getProductName())
                    .append(" x ")
                    .append(item.getQuantity())
                    .append(" = Rs. ")
                    .append(String.format("%.2f", item.getSubtotal()))
                    .append("\n");
        }

        bill.append("--------------------------------\n");

        bill.append("Subtotal: Rs. ")
                .append(String.format("%.2f", subtotal))
                .append("\n");

        bill.append("Discount: Rs. ")
                .append(String.format("%.2f", discount))
                .append("\n");

        bill.append("FINAL TOTAL: Rs. ")
                .append(String.format("%.2f", total))
                .append("\n");

        bill.append("Date: ")
                .append(LocalDate.now().format(
                        DateTimeFormatter.ofPattern("dd-MM-yyyy")
                ));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Confirm Checkout");
        confirm.setHeaderText("Complete this bill?");

        TextArea preview = new TextArea(bill.toString());
        preview.setEditable(false);
        preview.setWrapText(true);
        preview.setPrefSize(420, 300);

        confirm.getDialogPane().setContent(preview);

        confirm.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                for (CartItem item : cart.getCartItems()) {

                    boolean updated = inventory.updateStock(
                            item.getProductId(),
                            item.getQuantity()
                    );

                    if (!updated) {
                        showMessage(
                                "Checkout stopped because stock could not be updated."
                        );
                        refreshInventory();
                        return;
                    }
                }

                for (CartItem item : cart.getCartItems()) {

                    double lineAmount = item.getSubtotal();

                    if (subtotal > 0 && discount > 0) {
                        lineAmount -= discount *
                                (item.getSubtotal() / subtotal);
                    }

                    sales.recordSale(
                            item.getProductName(),
                            item.getQuantity(),
                            lineAmount
                    );
                }

                cart.clearCart();

                refreshCart();
                refreshInventory();
                refreshProductChoices();

                Alert success = new Alert(
                        Alert.AlertType.INFORMATION
                );

                success.setTitle("Bill Generated");
                success.setHeaderText(
                        "Payment completed successfully!"
                );
                success.setContentText(bill.toString());

                success.showAndWait();
            }
        });
    }
	    // ==========================================
    // EXPIRY REPORT
    // ==========================================

    private void showExpiryReport() {

        setPage(
                "Expiry Report",
                "Products that have expired or are expiring within 30 days."
        );

        TextArea report = new TextArea();

        report.setEditable(false);
        report.setWrapText(true);
        report.setPrefHeight(400);

        StringBuilder result = new StringBuilder();

        LocalDate today = LocalDate.now();

        for (Product p : inventory.getProducts()) {

            if (p.getExpiryDate() == null) {
                continue;
            }

            if (!p.getExpiryDate().isAfter(today.plusDays(30))) {

                result.append("Product: ")
                        .append(p.getName())
                        .append("\nQuantity: ")
                        .append(p.getQuantity())
                        .append("\nExpiry Date: ")
                        .append(p.getExpiryDate())
                        .append("\nStatus: ")
                        .append(p.getExpiryStatus())
                        .append("\nSuggested Discount: ")
                        .append(expiryManager.getSuggestedDiscount(p))
                        .append("%\n")
                        .append("--------------------------------\n");
            }
        }

        if (result.length() == 0) {
            result.append("No expired or soon-to-expire products found.");
        }

        report.setText(result.toString());

        VBox panel = panelBox();

        panel.getChildren().addAll(
                sectionTitle("Expiry Alerts"),
                report
        );

        content.getChildren().add(panel);
    }

    // ==========================================
    // SALES REPORT
    // ==========================================

    private void showSalesReport() {

        setPage(
                "Sales Report",
                "Review your recorded sales and revenue."
        );

        TextArea report = new TextArea();

        report.setEditable(false);
        report.setWrapText(true);
        report.setPrefHeight(400);

        report.setText(sales.getSalesReport());

        Label totalRevenueLabel = new Label(
                "Total Revenue: Rs. "
                        + String.format("%.2f", sales.getTotalRevenue())
        );

        totalRevenueLabel.setStyle(
                "-fx-text-fill: #63D6B0;" +
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        VBox panel = panelBox();

        panel.getChildren().addAll(
                sectionTitle("Sales History"),
                totalRevenueLabel,
                report
        );

        content.getChildren().add(panel);
    }

    // ==========================================
    // ANALYTICS AND EXPIRY MANAGEMENT
    // ==========================================

    private void showAnalyticsAndExpiry() {

        setPage(
                "Analytics & Expiry Management",
                "Daily sales performance, expiry discounts and stock management."
        );

        // DAILY SALES GRAPH

        VBox graphPanel = panelBox();

        graphPanel.getChildren().add(
                sectionTitle("Daily Sales Analysis")
        );

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Sales Date");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue (Rs.)");
        yAxis.setForceZeroInRange(true);

        BarChart<String, Number> salesChart =
                new BarChart<>(xAxis, yAxis);

        salesChart.setTitle("Day-by-Day Revenue");
        salesChart.setLegendVisible(false);
        salesChart.setAnimated(false);
        salesChart.setPrefHeight(300);

        XYChart.Series<String, Number> series =
                new XYChart.Series<>();

        Map<LocalDate, Double> dailySales =
                sales.getDailySalesTotals();

        for (Map.Entry<LocalDate, Double> entry
                : dailySales.entrySet()) {

            series.getData().add(
                    new XYChart.Data<>(
                            entry.getKey().toString(),
                            entry.getValue()
                    )
            );
        }

        salesChart.getData().add(series);

        Label revenueLabel = new Label(
                "Total Revenue: Rs. "
                        + String.format("%.2f", sales.getTotalRevenue())
        );

        revenueLabel.setStyle(
                "-fx-text-fill: #63D6B0;" +
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        graphPanel.getChildren().addAll(
                salesChart,
                revenueLabel
        );

        // EXPIRY DISCOUNT SUGGESTIONS

        VBox expiryPanel = panelBox();

        expiryPanel.getChildren().add(
                sectionTitle("Expiry Discount Suggestions")
        );

        TextArea expiryDetails = new TextArea();

        expiryDetails.setEditable(false);
        expiryDetails.setWrapText(true);
        expiryDetails.setPrefHeight(250);

        StringBuilder expiryText = new StringBuilder();

        for (Product p : inventory.getProducts()) {

            if (p.getExpiryDate() == null) {
                continue;
            }

            if (p.getExpiryDate().isAfter(
                    LocalDate.now().plusDays(30))) {
                continue;
            }

            double discount =
                    expiryManager.getSuggestedDiscount(p);

            double discountedPrice =
                    expiryManager.getDiscountedPrice(p);

            expiryText.append("Product: ")
                    .append(p.getName())
                    .append("\nQuantity: ")
                    .append(p.getQuantity())
                    .append("\nExpiry Date: ")
                    .append(p.getExpiryDate())
                    .append("\nStatus: ")
                    .append(p.getExpiryStatus())
                    .append("\nSuggested Discount: ")
                    .append(discount)
                    .append("%\nDiscounted Price: Rs. ")
                    .append(String.format("%.2f", discountedPrice))
                    .append("\n")
                    .append("--------------------------------\n");
        }

        if (expiryText.length() == 0) {
            expiryText.append(
                    "No products are expiring within 30 days."
            );
        }

        expiryDetails.setText(expiryText.toString());

        expiryPanel.getChildren().add(expiryDetails);

        // STOCK MANAGEMENT BUTTON

        Button stockManagementButton =
                actionButton("Open Stock Management");

        stockManagementButton.setOnAction(
                e -> showStockManagement()
        );

        content.getChildren().addAll(
                graphPanel,
                expiryPanel,
                stockManagementButton
        );
    }
	    // ==========================================
    // STOCK MANAGEMENT PAGE
    // ==========================================

    private void showStockManagement() {

        setPage(
                "Stock Management",
                "Record damaged stock, expired stock and customer returns."
        );

        Label productLabel = new Label("Select Product");
        productLabel.setStyle("-fx-text-fill: white;");

        ComboBox<String> productChoice =
                new ComboBox<>();

        productChoice.setPromptText("Choose a product");
        productChoice.setMaxWidth(Double.MAX_VALUE);

        for (Product p : inventory.getProducts()) {
            productChoice.getItems().add(p.getName());
        }

        Label quantityLabel = new Label("Quantity");
        quantityLabel.setStyle("-fx-text-fill: white;");

        Spinner<Integer> quantitySpinner =
                new Spinner<>(1, 100000, 1);

        quantitySpinner.setEditable(true);
        quantitySpinner.setMaxWidth(Double.MAX_VALUE);

        Label reasonLabel = new Label("Reason");
        reasonLabel.setStyle("-fx-text-fill: white;");

        TextField reasonField = new TextField();
        reasonField.setPromptText("Enter reason");
        reasonField.setStyle(fieldStyle());

        TextArea historyArea = new TextArea();

        historyArea.setEditable(false);
        historyArea.setWrapText(true);
        historyArea.setPrefRowCount(12);

        historyArea.setStyle(
                "-fx-control-inner-background: #111827;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;"
        );

        // REMOVE STOCK BUTTON

        Button removeButton =
                dangerButton("Record Stock Removal");

        removeButton.setOnAction(e -> {

            String selectedName = productChoice.getValue();
            int quantity = quantitySpinner.getValue();
            String reason = reasonField.getText().trim();

            if (selectedName == null) {
                showMessage("Please select a product.");
                return;
            }

            if (reason.isEmpty()) {
                showMessage("Please enter a reason.");
                return;
            }

            Product selectedProduct = null;

            for (Product p : inventory.getProducts()) {

                if (p.getName().equals(selectedName)) {
                    selectedProduct = p;
                    break;
                }
            }

            if (selectedProduct == null) {
                showMessage("Product not found.");
                return;
            }

            if (quantity > selectedProduct.getQuantity()) {
                showMessage("Not enough stock available.");
                return;
            }

            boolean success =
                    expiryManager.recordRemovedStock(
                            inventory,
                            selectedProduct,
                            quantity,
                            reason
                    );

            if (success) {

                refreshInventory();
                productChoice.getItems().clear();

                for (Product p : inventory.getProducts()) {
                    productChoice.getItems().add(p.getName());
                }

                historyArea.setText(
                        expiryManager.getRemovedStockReport()
                );

                showMessage(
                        "Stock removal recorded successfully."
                );
            }
        });

        // CUSTOMER RETURN BUTTON

        Button returnButton =
                secondaryButton("Record Customer Return");

        returnButton.setOnAction(e -> {

            String selectedName = productChoice.getValue();
            int quantity = quantitySpinner.getValue();
            String reason = reasonField.getText().trim();

            if (selectedName == null) {
                showMessage("Please select a product.");
                return;
            }

            if (reason.isEmpty()) {
                showMessage("Please enter a reason.");
                return;
            }

            Product selectedProduct = null;

            for (Product p : inventory.getProducts()) {

                if (p.getName().equals(selectedName)) {
                    selectedProduct = p;
                    break;
                }
            }

            if (selectedProduct == null) {
                showMessage("Product not found.");
                return;
            }

            boolean success =
                    expiryManager.recordReturnedStock(
                            inventory,
                            selectedProduct,
                            quantity,
                            reason
                    );

            if (success) {

                refreshInventory();
                productChoice.getItems().clear();

                for (Product p : inventory.getProducts()) {
                    productChoice.getItems().add(p.getName());
                }

                historyArea.setText(
                        expiryManager.getReturnedStockReport()
                );

                showMessage(
                        "Customer return recorded successfully."
                );
            }
        });

        // HISTORY BUTTONS

        Button removedHistoryButton =
                secondaryButton("View Removed Stock History");

        removedHistoryButton.setOnAction(e -> {

            historyArea.setText(
                    expiryManager.getRemovedStockReport()
            );
        });

        Button returnedHistoryButton =
                secondaryButton("View Returned Stock History");

        returnedHistoryButton.setOnAction(e -> {

            historyArea.setText(
                    expiryManager.getReturnedStockReport()
            );
        });

        // FORM PANEL

        VBox formPanel = panelBox();

        formPanel.getChildren().addAll(
                sectionTitle("Record Stock Movement"),
                productLabel,
                productChoice,
                quantityLabel,
                quantitySpinner,
                reasonLabel,
                reasonField,
                removeButton,
                returnButton
        );

        // HISTORY PANEL

        VBox historyPanel = panelBox();

        historyPanel.getChildren().addAll(
                sectionTitle("Stock Movement History"),
                removedHistoryButton,
                returnedHistoryButton,
                historyArea
        );

        HBox mainLayout = new HBox(
                20,
                formPanel,
                historyPanel
        );

        mainLayout.setPadding(new Insets(10));

        HBox.setHgrow(formPanel, Priority.ALWAYS);
        HBox.setHgrow(historyPanel, Priority.ALWAYS);

        content.getChildren().addAll(
                sectionTitle("Stock Movement"),
                mainLayout
        );
    }
	    // ==========================================
    // COMMON DESIGN AND STYLE METHODS
    // ==========================================

    private VBox panelBox() {

        VBox panel = new VBox(12);

        panel.setPadding(new Insets(20));

        panel.setStyle(
                "-fx-background-color: #1e293b;" +
                "-fx-background-radius: 12;"
        );

        return panel;
    }

    private Label sectionTitle(String title) {

        Label label = new Label(title);

        label.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;"
        );

        return label;
    }

    private Button actionButton(String text) {

        Button button = new Button(text);

        button.setMaxWidth(Double.MAX_VALUE);

        button.setStyle(
                "-fx-background-color: #2563eb;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-background-radius: 7;" +
                "-fx-cursor: hand;"
        );

        return button;
    }

    private Button secondaryButton(String text) {

        Button button = new Button(text);

        button.setMaxWidth(Double.MAX_VALUE);

        button.setStyle(
                "-fx-background-color: #0f766e;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-background-radius: 7;" +
                "-fx-cursor: hand;"
        );

        return button;
    }

    private Button dangerButton(String text) {

        Button button = new Button(text);

        button.setMaxWidth(Double.MAX_VALUE);

        button.setStyle(
                "-fx-background-color: #dc2626;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-background-radius: 7;" +
                "-fx-cursor: hand;"
        );

        return button;
    }

    private String fieldStyle() {

        return
                "-fx-background-color: #111827;" +
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #94a3b8;" +
                "-fx-border-color: #475569;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;" +
                "-fx-padding: 9;";
    }

    private void showMessage(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Smart Inventory System");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    // ==========================================
    // APPLICATION STARTING POINT
    // ==========================================

    public static void main(String[] args) {

        launch(args);
    }

}