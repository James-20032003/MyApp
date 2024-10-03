import javax.swing.*;  
import javax.swing.table.DefaultTableModel;  
import java.awt.*;  
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;  
import java.sql.*;  
import java.util.ArrayList;  
import java.util.List;  

public class CropManagementApp extends JFrame {  
    private DefaultTableModel tableModel;  
    private JTable cropTable;  
    private JTextArea messageArea;  

    // In-memory user storage (use a database in production)  
    private List<User> users = new ArrayList<>();  
    private User loggedInUser;  

    public CropManagementApp() {  
        setTitle("Crop Management System");  
        setSize(800, 600);  
        setDefaultCloseOperation(EXIT_ON_CLOSE);  
        setLayout(new BorderLayout());  

        // User Authentication  
        if (!showLoginDialog()) {  
            System.exit(0); // Exit if login fails  
        }  

        initializeUI();  
        loadData();  
    }  

    private void initializeUI() {  
        // Table for displaying crops  
        String[] columnNames = {"Crop ID", "Crop Name", "Crop Type", "Planting Date", "Harvest Date", "Status"};  
        tableModel = new DefaultTableModel(columnNames, 0);  
        cropTable = new JTable(tableModel);  
        JScrollPane messageScrollPane = new JScrollPane(cropTable);  

        // Messaging area  
        messageArea = new JTextArea(5, 20);  
        messageArea.setEditable(false);  
        JScrollPane messageScroll = new JScrollPane(messageArea);  

        // Button panel for CRUD operations  
        JPanel buttonPanel = new JPanel();  
        JButton addButton = new JButton("Add Crop");  
        addButton.addActionListener(e -> openAddCropDialog());  
        JButton updateButton = new JButton("Update Crop");  
        updateButton.addActionListener(e -> openUpdateCropDialog());  
        JButton deleteButton = new JButton("Delete Crop");  
        deleteButton.addActionListener(e -> openDeleteCropDialog());  
        buttonPanel.add(addButton);  
        buttonPanel.add(updateButton);  
        buttonPanel.add(deleteButton);  

        // Messaging panel  
        JTextField inputField = new JTextField(20);  
        JButton sendButton = new JButton("Send");  
        sendButton.addActionListener(e -> {  
            String userInput = inputField.getText();  
            sendMessage(userInput);  
            inputField.setText("");  
        });  

        // Assemble messaging panel  
        JPanel messagingPanel = new JPanel();  
        messagingPanel.setLayout(new BorderLayout());  
        messagingPanel.add(messageScroll, BorderLayout.CENTER);  

        JPanel inputPanel = new JPanel();  
        inputPanel.add(inputField);  
        inputPanel.add(sendButton);  
        messagingPanel.add(inputPanel, BorderLayout.SOUTH);  

        // Add components to frame  
        add(new JScrollPane(cropTable), BorderLayout.CENTER);  
        add(buttonPanel, BorderLayout.SOUTH);  
        add(messagingPanel, BorderLayout.NORTH); // Add messaging panel to the top  
    }  

    // User authentication methods  
    private boolean showLoginDialog() {  
        JTextField usernameField = new JTextField();  
        JPasswordField passwordField = new JPasswordField();  
        Object[] message = {  
                "Username:", usernameField,  
                "Password:", passwordField  
        };  

        int option = JOptionPane.showConfirmDialog(this, message, "Login", JOptionPane.OK_CANCEL_OPTION);  
        if (option == JOptionPane.OK_OPTION) {  
            String username = usernameField.getText();  
            String password = String.valueOf(passwordField.getPassword());  
            return login(username, password);  
        } else {  
            // Show signup if canceled  
            showSignUpDialog();  
            return true;  
        }  
    }  

    private void showSignUpDialog() {  
        JTextField usernameField = new JTextField();  
        JPasswordField passwordField = new JPasswordField();  
        Object[] message = {  
                "Choose a username:", usernameField,  
                "Choose a password:", passwordField  
        };  

        int option = JOptionPane.showConfirmDialog(this, message, "Sign Up", JOptionPane.OK_CANCEL_OPTION);  
        if (option == JOptionPane.OK_OPTION) {  
            String username = usernameField.getText();  
            String password = String.valueOf(passwordField.getPassword());  
            signUp(username, password);  
        }  
    }  

    private void signUp(String username, String password) {  
        // Simulate user registration  
        // (In production, you would store this in a database)  
        users.add(new User(username, password));  
        JOptionPane.showMessageDialog(this, "User registered successfully! You can now log in.");  
        showLoginDialog();  
    }  

    private boolean login(String username, String password) {  
        for (User user : users) {  
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {  
                loggedInUser = user;  
                JOptionPane.showMessageDialog(this, "Welcome, " + username + "!");  
                return true; // Successful login  
            }  
        }  
        JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.");  
        return false;  
    }  

    // Method to load data from the database into the table  
    private void loadData() {  
        tableModel.setRowCount(0); // Clear existing rows  
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriculture", "root", "password");  
             Statement stmt = conn.createStatement();  
             ResultSet rs = stmt.executeQuery("SELECT * FROM crops")) {  

            while (rs.next()) {  
                tableModel.addRow(new Object[]{  
                        rs.getInt("crop_id"),  
                        rs.getString("crop_name"),  
                        rs.getString("crop_type"),  
                        rs.getDate("planting_date"),  
                        rs.getDate("harvest_date"),  
                        rs.getString("status")  
                });  
            }  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  
    }  

    // Handle sending messages  
    private void sendMessage(String message) {  
        messageArea.append("You: " + message + "\n");  
        String response = getAIResponse(message);  
        messageArea.append("AI: " + response + "\n");  
    }  

    // Basic AI response simulation  
    private String getAIResponse(String message) {  
        if (message.toLowerCase().contains("hello")) {  
            return "Hello! How can I assist you today?";  
        } else if (message.toLowerCase().contains("crop status")) {  
            return "You can check the status of your crops in the table below.";  
        } else if (message.toLowerCase().contains("help")) {  
            return "What kind of help do you need?";  
        } else {  
            return "I'm not sure how to respond to that. Can you ask something else?";  
        }  
    }  

    // Method to open dialog for adding new crops  
    private void openAddCropDialog() {  
        CropDialog dialog = new CropDialog(this, "Add Crop", null);  
        dialog.setVisible(true);  
    }  

    // Method to open dialog for updating existing crops  
    private void openUpdateCropDialog() {  
        int selectedRow = cropTable.getSelectedRow();  
        if (selectedRow != -1) {  
            int cropId = (int) tableModel.getValueAt(selectedRow, 0);  
            CropDialog dialog = new CropDialog(this, "Update Crop", cropId);  
            dialog.setVisible(true);  
        } else {  
            JOptionPane.showMessageDialog(this, "Select a crop to update.");  
        }  
    }  

    // Method for deleting crops  
    private void openDeleteCropDialog() {  
        int selectedRow = cropTable.getSelectedRow();  
        if (selectedRow != -1) {  
            int cropId = (int) tableModel.getValueAt(selectedRow, 0);  
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this crop?", "Confirm Delete", JOptionPane.YES_NO_OPTION);  
            if (confirm == JOptionPane.YES_OPTION) {  
                deleteCrop(cropId);  
                loadData(); // Refresh data after deletion  
            }  
        } else {  
            JOptionPane.showMessageDialog(this, "Select a crop to delete.");  
        }  
    }  

    // Method for deleting a crop from the database  
    private void deleteCrop(int cropId) {  
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriculture", "root", "password");  
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM crops WHERE crop_id = ?")) {  
            pstmt.setInt(1, cropId);  
            pstmt.executeUpdate();  
            JOptionPane.showMessageDialog(this, "Crop deleted successfully!");  
        } catch (SQLException e) {  
            e.printStackTrace();  
            JOptionPane.showMessageDialog(this, "Error deleting crop.");  
        }  
    }  

    public static void main(String[] args) {  
        SwingUtilities.invokeLater(() -> {  
            new CropManagementApp().setVisible(true);  
        });  
    }  

    // Nested class for Crop dialog for adding/updating crops  
    private class CropDialog extends JDialog {  
        private JTextField cropNameField, cropTypeField, plantingDateField, harvestDateField;  
        private JComboBox<String> statusComboBox;  
        private Integer cropId;  

        public CropDialog(Frame parent, String title, Integer cropId) {  
            super(parent, title, true);  
            this.cropId = cropId;  
            setLayout(new GridLayout(6, 2));  
            setSize(400, 300);  

            cropNameField = new JTextField();  
            cropTypeField = new JTextField();  
            plantingDateField = new JTextField();  
            harvestDateField = new JTextField();  
            statusComboBox = new JComboBox<>(new String[]{"Planted", "Harvested", "Lost"});  

            add(new JLabel("Crop Name:"));  
            add(cropNameField);  
            add(new JLabel("Crop Type:"));  
            add(cropTypeField);  
            add(new JLabel("Planting Date (YYYY-MM-DD):"));  
            add(plantingDateField);  
            add(new JLabel("Harvest Date (YYYY-MM-DD):"));  
            add(harvestDateField);  
            add(new JLabel("Status:"));  
            add(statusComboBox);  

            JButton submitButton = new JButton(cropId == null ? "Add" : "Update");  
            submitButton.addActionListener(e -> {  
                if (cropId == null) {  
                    addCrop();  
                } else {  
                    updateCrop(cropId);  
                }  
            });  

            add(submitButton);  
        }  

        // Method to add crop to the database  
        private void addCrop() {  
            String name = cropNameField.getText().trim();  
            String type = cropTypeField.getText().trim();  
            String plantingDate = plantingDateField.getText().trim();  
            String harvestDate = harvestDateField.getText().trim();  
            String status = (String) statusComboBox.getSelectedItem();  

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriculture", "root", "password");  
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO crops (crop_name, crop_type, planting_date, harvest_date, status) VALUES (?, ?, ?, ?, ?)")) {  
                pstmt.setString(1, name);  
                pstmt.setString(2, type);  
                pstmt.setDate(3, Date.valueOf(plantingDate));  
                pstmt.setDate(4, harvestDate.isEmpty() ? null : Date.valueOf(harvestDate));  
                pstmt.setString(5, status);  
                pstmt.executeUpdate();  
                JOptionPane.showMessageDialog(this, "Crop added successfully!");  
                loadData(); // Refresh table data  
                dispose(); // Close dialog  
            } catch (SQLException e) {  
                e.printStackTrace();  
                JOptionPane.showMessageDialog(this, "Error adding crop.");  
            }  
        }  

        // Method to update crop in the database  
        private void updateCrop(int cropId) {  
            String name = cropNameField.getText().trim();  
            String type = cropTypeField.getText().trim();  
            String plantingDate = plantingDateField.getText().trim();  
            String harvestDate = harvestDateField.getText().trim();  
            String status = (String) statusComboBox.getSelectedItem();  

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriculture", "root", "password");  
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE crops SET crop_name = ?, crop_type = ?, planting_date = ?, harvest_date = ?, status = ? WHERE crop_id = ?")) {  
                pstmt.setString(1, name);  
                pstmt.setString(2, type);  
                pstmt.setDate(3, Date.valueOf(plantingDate));  
                pstmt.setDate(4, harvestDate.isEmpty() ? null : Date.valueOf(harvestDate));  
                pstmt.setString(5, status);  
                pstmt.setInt(6, cropId);  
                pstmt.executeUpdate();  
                JOptionPane.showMessageDialog(this, "Crop updated successfully!");  
                loadData(); // Refresh table data  
                dispose(); // Close dialog  
            } catch (SQLException e) {  
                e.printStackTrace();  
                JOptionPane.showMessageDialog(this, "Error updating crop.");  
            }  
        }  
    }  

    // User class for handling user data  
    private class User {  
        private String username;  
        private String password;  

        public User(String username, String password) {  
            this.username = username;  
            this.password = password;  
        }  

        public String getUsername() {  
            return username;  
        }  

        public String getPassword() {  
            return password;  
        }  
    }  
}