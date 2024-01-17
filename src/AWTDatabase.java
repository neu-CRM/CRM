import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

class Customer {
    int id;
    String name;
    String address;
    String phone;

    public Customer(int id, String name, String address, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }
}

public class AWTDatabase extends JFrame {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/crmdb";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private JPanel customerPanel;
    private JPanel ticketPanel;
    private JTextArea customerResultArea;
    private JTextArea ticketResultArea;

    public AWTDatabase() {
        createDatabaseIfNotExists();
        createTablesIfNotExist();

        setTitle("CRM Rendszer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        customerPanel = createCustomerPanel();
        ticketPanel = createTicketPanel();

        tabbedPane.addTab("Ügyféladatok", customerPanel);
        tabbedPane.addTab("Ticketek kezelése", ticketPanel);

        add(tabbedPane);
        setVisible(true);
    }

    private void createDatabaseIfNotExists() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306", USERNAME, PASSWORD);
            Statement statement = connection.createStatement()) {
            String createDatabaseQuery = "CREATE DATABASE IF NOT EXISTS crmdb CHARACTER SET utf8mb4 COLLATE utf8mb4_hungarian_ci;";
            statement.executeUpdate(createDatabaseQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Adatbázis létrehozása sikertelen! Hiba: " + e.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }    

    private void createTablesIfNotExist() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL + "?characterEncoding=utf8", USERNAME, PASSWORD)) {
            // Vásárló tábla létrehozására:
            String createTableQuery = "CREATE TABLE IF NOT EXISTS vasarlo (" +
                    "Vaz int(11) NOT NULL AUTO_INCREMENT," +
                    "Vnev varchar(255) COLLATE utf8_hungarian_ci DEFAULT NULL," +
                    "Vcim varchar(255) COLLATE utf8_hungarian_ci DEFAULT NULL," +
                    "Vtelszam varchar(50) COLLATE utf8_hungarian_ci NOT NULL," +
                    "PRIMARY KEY (Vaz)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
                preparedStatement.executeUpdate();
            }

            // Termék tábla létrehozására:
            createTableQuery = "CREATE TABLE IF NOT EXISTS termek (" +
                    "Taz int(11) NOT NULL AUTO_INCREMENT," +
                    "Tnev varchar(255) COLLATE utf8_hungarian_ci DEFAULT NULL," +
                    "PRIMARY KEY (Taz)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
                preparedStatement.executeUpdate();
            }
    
            // Eladás tábla létrehozása
            createTableQuery = "CREATE TABLE IF NOT EXISTS eladas (" +
                    "Vaz int(11) NOT NULL," +
                    "Taz int(11) NOT NULL," +
                    "Datum date DEFAULT NULL," +
                    "Ar double DEFAULT NULL," +
                    "EladID int(11) NOT NULL AUTO_INCREMENT," +
                    "PRIMARY KEY (EladID)," +
                    "KEY FK_Eladas_Taz (Taz)," +
                    "KEY FK_Eladas_Vaz (Vaz)," +
                    "CONSTRAINT FK_Eladas_Taz FOREIGN KEY (Taz) REFERENCES termek (Taz) ON DELETE NO ACTION," +
                    "CONSTRAINT FK_Eladas_Vaz FOREIGN KEY (Vaz) REFERENCES vasarlo (Vaz) ON DELETE NO ACTION" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
                preparedStatement.executeUpdate();
            }
    
            // Ticket tábla létrehozása
            createTableQuery = "CREATE TABLE IF NOT EXISTS ticket (" +
                    "Vaz int(11) NOT NULL," +
                    "TID int(11) NOT NULL AUTO_INCREMENT," +
                    "Message text COLLATE utf8_hungarian_ci DEFAULT NULL," +
                    "SubmitDate date DEFAULT NULL," +
                    "ThreadID int(11) DEFAULT NULL," +
                    "EladasID int(11) NOT NULL," +
                    "PRIMARY KEY (TID)," +
                    "KEY FK_Ticket_Vaz (Vaz)," +
                    "KEY FK_ticket_EladasID (EladasID)," +
                    "CONSTRAINT FK_Ticket_Vaz FOREIGN KEY (Vaz) REFERENCES vasarlo (Vaz) ON DELETE NO ACTION," +
                    "CONSTRAINT FK_ticket_EladasID FOREIGN KEY (EladasID) REFERENCES eladas (EladID) ON DELETE NO ACTION" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
                preparedStatement.executeUpdate();
            }
    
            // Kiszállítás tábla létrehozására:
            createTableQuery = "CREATE TABLE IF NOT EXISTS kiszallitas (" +
                    "SzalID int(11) NOT NULL AUTO_INCREMENT," +
                    "SzalDatum date DEFAULT NULL," +
                    "Megjegyzes text COLLATE utf8_hungarian_ci DEFAULT NULL," +
                    "Status varchar(255) COLLATE utf8_hungarian_ci DEFAULT NULL," +
                    "SzallitCim text COLLATE utf8_hungarian_ci DEFAULT NULL," +
                    "EladID int(11) NOT NULL," +
                    "PRIMARY KEY (SzalID)," +
                    "KEY FK_KiSzallitas_EladID (EladID)," +
                    "CONSTRAINT FK_KiSzallitas_EladID FOREIGN KEY (EladID) REFERENCES eladas (EladID) ON DELETE NO ACTION" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
                preparedStatement.executeUpdate();
            }
    
            // Partner tábla létrehozására:
            createTableQuery = "CREATE TABLE IF NOT EXISTS partner (" +
                    "Paz int(11) NOT NULL AUTO_INCREMENT," +
                    "Pnev varchar(255) COLLATE utf8_hungarian_ci DEFAULT NULL," +
                    "PRIMARY KEY (Paz)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
                preparedStatement.executeUpdate();
            }
    
            // Feladat tábla létrehozására:
            createTableQuery = "CREATE TABLE IF NOT EXISTS feladat (" +
                    "Vaz int(11) NOT NULL," +
                    "TID int(11) NOT NULL," +
                    "Leiras text COLLATE utf8_hungarian_ci DEFAULT NULL," +
                    "Hatarido date DEFAULT NULL," +
                    "Prioritas int(11) DEFAULT NULL," +
                    "FeladatID int(11) NOT NULL AUTO_INCREMENT," +
                    "PRIMARY KEY (FeladatID)," +
                    "KEY FK_Feladat_Vaz (Vaz)," +
                    "KEY FK_Feladat_TID (TID)," +
                    "CONSTRAINT FK_Feladat_Vaz FOREIGN KEY (Vaz) REFERENCES vasarlo (Vaz) ON DELETE NO ACTION," +
                    "CONSTRAINT FK_Feladat_TID FOREIGN KEY (TID) REFERENCES ticket (TID) ON DELETE NO ACTION" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Táblák létrehozása sikertelen! Hiba: " + e.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }  

    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber.matches("\\d{9,15}");
    }

    private boolean isPhoneNumberUnique(String phoneNumber) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT COUNT(*) FROM vasarlo WHERE Vtelszam = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, phoneNumber);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt(1) == 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean isPhoneNumberUniqueForUpdate(int customerId, String phoneNumber) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT COUNT(*) FROM vasarlo WHERE Vtelszam = ? AND Vaz != ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, phoneNumber);
                preparedStatement.setInt(2, customerId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt(1) == 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean isAnyFieldEmpty(String name, String address, String phone) {
        return name.isEmpty() || address.isEmpty() || phone.isEmpty();
    }

    private void insertCustomer(String name, String address, String phone) {
        if (isAnyFieldEmpty(name, address, phone)) {
            JOptionPane.showMessageDialog(null, "Minden mezőt ki kell tölteni!", "Hiba", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isPhoneNumberValid(phone)) {
            JOptionPane.showMessageDialog(null, "Érvénytelen telefonszám formátum!", "Hiba", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isPhoneNumberUnique(phone)) {
            JOptionPane.showMessageDialog(null, "Már létezik ügyfél ezzel a telefonszámmal!", "Hiba", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "INSERT INTO vasarlo (Vnev, Vcim, Vtelszam) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, address);
                preparedStatement.setString(3, phone);
                preparedStatement.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Ügyfél rögzítve sikeresen!", "Rögzítés", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Rögzítés sikertelen! Hiba: " + ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer(int customerId) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "DELETE FROM vasarlo WHERE Vaz = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, customerId);
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(null, "Ügyfél törölve sikeresen!", "Törlés", JOptionPane.INFORMATION_MESSAGE);
                    searchCustomers("");
                } else {
                    JOptionPane.showMessageDialog(null, "Ügyfél nem található a megadott azonosítóval!", "Törlés", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Törlés sikertelen! Hiba: " + ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomer(int customerId, String name, String address, String phone) {
        if (isAnyFieldEmpty(name, address, phone)) {
            JOptionPane.showMessageDialog(null, "Minden mezőt ki kell tölteni!", "Hiba", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            boolean hasChanges = !name.isEmpty() || !address.isEmpty() || !phone.isEmpty();

            if (hasChanges) {
                if (!isPhoneNumberUniqueForUpdate(customerId, phone)) {
                    JOptionPane.showMessageDialog(null, "Már létezik ügyfél ezzel a telefonszámmal!", "Hiba", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (areCustomerDetailsDifferent(customerId, name, address, phone)) {
                    String query = "UPDATE vasarlo SET Vnev = ?, Vcim = ?, Vtelszam = ? WHERE Vaz = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, name);
                        preparedStatement.setString(2, address);
                        preparedStatement.setString(3, phone);
                        preparedStatement.setInt(4, customerId);
                        int affectedRows = preparedStatement.executeUpdate();
                        if (affectedRows > 0) {
                            JOptionPane.showMessageDialog(null, "Ügyfél módosítva sikeresen!", "Módosítás", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Nem sikerült módosítani az ügyfelet. Ellenőrizze az azonosítót!", "Hiba", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Nincs változtatás az adatokban!", "Figyelmeztetés", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Nincs változtatás az adatokban!", "Figyelmeztetés", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Módosítás sikertelen! Hiba: " + ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean areCustomerDetailsDifferent(int customerId, String newName, String newAddress, String newPhone) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT Vnev, Vcim, Vtelszam FROM vasarlo WHERE Vaz = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, customerId);
                ResultSet resultSet = preparedStatement.executeQuery();
    
                if (resultSet.next()) {
                    String currentName = resultSet.getString("Vnev");
                    String currentAddress = resultSet.getString("Vcim");
                    String currentPhone = resultSet.getString("Vtelszam");
    
                    return !currentName.equals(newName) || !currentAddress.equals(newAddress) || !currentPhone.equals(newPhone);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    private JPanel createCustomerPanel() {
        // Ügyféladatok panel
        JPanel customerPanel = new JPanel();
        customerPanel.setLayout(null);
    
        JLabel nameLabel = new JLabel("Név:");
        nameLabel.setBounds(50, 50, 80, 25);
        customerPanel.add(nameLabel);
    
        JTextField nameText = new JTextField();
        nameText.setBounds(150, 50, 280, 25);
        customerPanel.add(nameText);
    
        JLabel addressLabel = new JLabel("Cím:");
        addressLabel.setBounds(50, 90, 80, 25);
        customerPanel.add(addressLabel);
    
        JTextField addressText = new JTextField();
        addressText.setBounds(150, 90, 280, 25);
        customerPanel.add(addressText);
    
        JLabel phoneLabel = new JLabel("Telefonszám:");
        phoneLabel.setBounds(50, 130, 100, 25);
        customerPanel.add(phoneLabel);
    
        JTextField phoneText = new JTextField();
        phoneText.setBounds(150, 130, 280, 25);
        customerPanel.add(phoneText);
    
        JButton addButton = new JButton("Beszúrás");
        addButton.setBounds(50, 180, 100, 30);
        customerPanel.add(addButton);
    
        JButton deleteButton = new JButton("Töröl");
        deleteButton.setBounds(160, 180, 100, 30);
        customerPanel.add(deleteButton);
    
        JButton updateButton = new JButton("Módosítás");
        updateButton.setBounds(270, 180, 100, 30);
        customerPanel.add(updateButton);
    
        customerResultArea = new JTextArea();
        customerResultArea.setBounds(50, 220, 380, 250);
        customerPanel.add(customerResultArea);
    
        // Beszúrás gomb eseménykezelője
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameText.getText();
                String address = addressText.getText();
                String phone = phoneText.getText();
                insertCustomer(name, address, phone);
                searchCustomers("");
            }
        });
    
        // Törlés gomb eseménykezelője
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(null, "Adja meg az ügyfél azonosítóját (ID):", "Ügyfél törlése", JOptionPane.QUESTION_MESSAGE);
                if (input != null && !input.isEmpty()) {
                    try {
                        int customerId = Integer.parseInt(input);
                        deleteCustomer(customerId);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Érvénytelen azonosító formátum!", "Hiba", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Ügyfél törlése megszakítva.", "Figyelmeztetés", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    
        // Módosítás gomb eseménykezelője
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(null, "Adja meg az ügyfél azonosítóját (ID) a módosításhoz:", "Ügyfél módosítása", JOptionPane.QUESTION_MESSAGE);
                if (input != null && !input.isEmpty()) {
                    try {
                        int customerId = Integer.parseInt(input);
                        String name = nameText.getText();
                        String address = addressText.getText();
                        String phone = phoneText.getText();
        
                        if (!name.equals("") || !address.equals("") || !phone.equals("")) {
                            updateCustomer(customerId, name, address, phone);
                            searchCustomers("");
                        } else {
                            JOptionPane.showMessageDialog(null, "Nincs változtatás az adatokban!", "Figyelmeztetés", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Érvénytelen azonosító formátum!", "Hiba", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Módosítás megszakítva.", "Figyelmeztetés", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
            
        searchCustomers("");

        return customerPanel;
    }
        
    private JPanel createTicketPanel() {
        // Ticketek kezelése panel
        JPanel ticketPanel = new JPanel();
        ticketPanel.setLayout(null);
    
        JLabel ticketNumberLabel = new JLabel("Ticket szám:");
        ticketNumberLabel.setBounds(50, 50, 100, 25);
        ticketPanel.add(ticketNumberLabel);
    
        JTextField ticketNumberText = new JTextField();
        ticketNumberText.setBounds(150, 50, 280, 25);
        ticketPanel.add(ticketNumberText);
    
        JLabel issueLabel = new JLabel("Probléma leírása:");
        issueLabel.setBounds(50, 90, 120, 25);
        ticketPanel.add(issueLabel);
    
        JTextField issueText = new JTextField();
        issueText.setBounds(150, 90, 280, 25);
        ticketPanel.add(issueText);
    
        JButton createTicketButton = new JButton("Ticket létrehozása");
        createTicketButton.setBounds(150, 140, 180, 30);
        ticketPanel.add(createTicketButton);
    
        ticketResultArea = new JTextArea();
        ticketResultArea.setBounds(50, 180, 380, 250);
        ticketPanel.add(ticketResultArea);
    
        return ticketPanel;
    }
        
    private void searchCustomers(String searchTerm) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT * FROM vasarlo WHERE Vnev LIKE ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, "%" + searchTerm + "%");
                ResultSet resultSet = preparedStatement.executeQuery();
    
                customerResultArea.setText("");
                while (resultSet.next()) {
                    int id = resultSet.getInt("Vaz");
                    String name = resultSet.getString("Vnev");
                    String address = resultSet.getString("Vcim");
                    String phone = resultSet.getString("Vtelszam");
    
                    customerResultArea.append("ID: " + id + "\n");
                    customerResultArea.append("Név: " + name + "\n");
                    customerResultArea.append("Cím: " + address + "\n");
                    customerResultArea.append("Telefonszám: " + phone + "\n");
                    customerResultArea.append("\n");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AWTDatabase());
    }
}        