/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appintegration_fernandez;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AppIntegration_Fernandez extends JFrame {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/electricitybilling";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    private Connection connection;

    private JButton addCustomerButton;
    private JButton addUsageButton;
    private JButton generateBillButton;
    private JButton viewBillsButton;
    private JButton searchCustomerButton;
    private JButton exitButton;
    private JTextArea outputArea;

    public AppIntegration_Fernandez() {
        // Set up frame properties
        setTitle("Electricity Bill Tracker");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize the components
        addCustomerButton = new JButton("Add Customer");
        addUsageButton = new JButton("Add Usage");
        generateBillButton = new JButton("Generate Bill");
        viewBillsButton = new JButton("View Bills");
        searchCustomerButton = new JButton("Search Customer");
        exitButton = new JButton("Exit");
        outputArea = new JTextArea();
        outputArea.setEditable(false);

        // Set up layout
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1));
        buttonPanel.add(addCustomerButton);
        buttonPanel.add(addUsageButton);
        buttonPanel.add(generateBillButton);
        buttonPanel.add(viewBillsButton);
        buttonPanel.add(searchCustomerButton);
        buttonPanel.add(exitButton);

        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(buttonPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);

        // Connect to the database
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            outputArea.append("Database connection failed.\n");
        }

        // Add action listeners for buttons
        addCustomerButton.addActionListener(e -> addCustomer());
        addUsageButton.addActionListener(e -> addUsage());
        generateBillButton.addActionListener(e -> generateBill());
        viewBillsButton.addActionListener(e -> viewBills());
        searchCustomerButton.addActionListener(e -> searchCustomer());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void addCustomer() {
        String name = JOptionPane.showInputDialog(this, "Enter Name:");
        String address = JOptionPane.showInputDialog(this, "Enter Address:");
        String phoneNumber = JOptionPane.showInputDialog(this, "Enter Phone Number:");

        String sql = "INSERT INTO Customers (Name, Address, PhoneNumber) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, address);
            stmt.setString(3, phoneNumber);
            stmt.executeUpdate();
            outputArea.append("Customer added successfully.\n");
        } catch (SQLException e) {
            e.printStackTrace();
            outputArea.append("Error adding customer.\n");
        }
    }

    private void addUsage() {
        int customerId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Customer ID:"));
        int unitsUsed = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Units Used:"));
        String month = JOptionPane.showInputDialog(this, "Enter Month:");
        int year = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Year:"));

        String sql = "INSERT INTO UsageRate (CustomerID, UnitsUsed, Month, Year) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, unitsUsed);
            stmt.setString(3, month);
            stmt.setInt(4, year);
            stmt.executeUpdate();
            outputArea.append("Electricity usage added successfully.\n");
        } catch (SQLException e) {
            e.printStackTrace();
            outputArea.append("Error adding usage.\n");
        }
    }

    private void generateBill() {
        int usageId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Usage ID:"));

        String usageQuery = "SELECT CustomerID, UnitsUsed FROM UsageRate WHERE UsageID = ?";
        try (PreparedStatement usageStmt = connection.prepareStatement(usageQuery)) {
            usageStmt.setInt(1, usageId);
            ResultSet usageResult = usageStmt.executeQuery();

            if (usageResult.next()) {
                int customerId = usageResult.getInt("CustomerID");
                int unitsUsed = usageResult.getInt("UnitsUsed");
                double ratePerUnit = 5.0; // Example rate
                double totalAmount = unitsUsed * ratePerUnit;

                String billInsert = "INSERT INTO Bills (CustomerID, UsageID, TotalAmount) VALUES (?, ?, ?)";
                try (PreparedStatement billStmt = connection.prepareStatement(billInsert)) {
                    billStmt.setInt(1, customerId);
                    billStmt.setInt(2, usageId);
                    billStmt.setDouble(3, totalAmount);
                    billStmt.executeUpdate();
                    outputArea.append("Bill generated successfully. Total Amount: " + totalAmount + "\n");
                }
            } else {
                outputArea.append("Usage ID not found.\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            outputArea.append("Error generating bill.\n");
        }
    }

    private void viewBills() {
        String sql = "SELECT b.BillID, c.Name, u.Month, u.Year, b.TotalAmount " +
                     "FROM Bills b " +
                     "JOIN Customers c ON b.CustomerID = c.CustomerID " +
                     "JOIN UsageRate u ON b.UsageID = u.UsageID";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            outputArea.append("\n=== Bills ===\n");
            while (rs.next()) {
                outputArea.append(String.format("Bill ID: %d, Customer: %s, Month: %s, Year: %d, Amount: %.2f\n",
                        rs.getInt("BillID"), rs.getString("Name"),
                        rs.getString("Month"), rs.getInt("Year"),
                        rs.getDouble("TotalAmount")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            outputArea.append("Error viewing bills.\n");
        }
    }

    private void searchCustomer() {
        String name = JOptionPane.showInputDialog(this, "Enter Customer Name:");

        String sql = "SELECT * FROM Customers WHERE Name LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            outputArea.append("\n=== Customer Search Results ===\n");
            while (rs.next()) {
                outputArea.append(String.format("ID: %d, Name: %s, Address: %s, Phone: %s\n",
                        rs.getInt("CustomerID"), rs.getString("Name"),
                        rs.getString("Address"), rs.getString("PhoneNumber")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            outputArea.append("Error searching customer.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AppIntegration_Fernandez().setVisible(true);
        });
    }
}
