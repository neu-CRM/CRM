import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ResourceBundle;

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
    private ResourceBundle messages;

    private JPanel customerPanel;
    private JPanel ticketPanel;
    private JTextArea customerResultArea;
    private JTextArea ticketResultArea;

    private void setLanguage(Locale locale) {
        messages = ResourceBundle.getBundle("languages.messages", locale);
    }

    public AWTDatabase(Locale locale) {
        setLanguage(locale);
        createDatabaseIfNotExists();
        createTablesIfNotExist();

        setTitle(messages.getString("window.mainTitle"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        customerPanel = createCustomerPanel();
        ticketPanel = createTicketPanel();

        tabbedPane.addTab(messages.getString("customer.tab.name"), customerPanel);
        tabbedPane.addTab(messages.getString("ticket.tab.name"), ticketPanel);

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
            JOptionPane.showMessageDialog(null, messages.getString("messages.failedDatabaseCreation") + e.getMessage(), messages.getString("window.errorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void createTablesIfNotExist() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL + "?characterEncoding=utf8", USERNAME,
                PASSWORD)) {
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
                    "CONSTRAINT FK_ticket_EladasID FOREIGN KEY (EladasID) REFERENCES eladas (EladID) ON DELETE NO ACTION"
                    +
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
                    "CONSTRAINT FK_KiSzallitas_EladID FOREIGN KEY (EladID) REFERENCES eladas (EladID) ON DELETE NO ACTION"
                    +
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
            JOptionPane.showMessageDialog(null, messages.getString("messages.failedTableCreation") + e.getMessage(), messages.getString("window.errorTitle"),
                    JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(null, messages.getString("messages.allFeildsRequired"), messages.getString("window.errorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isPhoneNumberValid(phone)) {
            JOptionPane.showMessageDialog(null, messages.getString("messages.invalidCustomerPhoneFormat"), messages.getString("window.errorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isPhoneNumberUnique(phone)) {
            JOptionPane.showMessageDialog(null, messages.getString("messages.customerPhoneNumberAlreadyExists"), messages.getString("window.errorTitle"),JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(null, messages.getString("messages.customerSuccessfullyRecorded"), messages.getString("window.savingTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, messages.getString("messages.customerRecordFailed") + ex.getMessage(), messages.getString("window.errorTitle"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer(int customerId) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "DELETE FROM vasarlo WHERE Vaz = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, customerId);
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(null, messages.getString("messages.customerDeletedSuccessfully"), messages.getString("window.deleting"),
                            JOptionPane.INFORMATION_MESSAGE);
                    searchCustomers("");
                } else {
                    JOptionPane.showMessageDialog(null, messages.getString("messages.failedTofindCustomerWithID"), messages.getString("window.deleting"),
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, messages.getString("messages.customerDeleteFailed") + ex.getMessage(), messages.getString("window.errorTitle"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomer(int customerId, String name, String address, String phone) {
        if (isAnyFieldEmpty(name, address, phone)) {
            JOptionPane.showMessageDialog(null, messages.getString("messages.allFeildsRequired"), messages.getString("window.errorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            boolean hasChanges = !name.isEmpty() || !address.isEmpty() || !phone.isEmpty();

            if (hasChanges) {
                if (!isPhoneNumberUniqueForUpdate(customerId, phone)) {
                    JOptionPane.showMessageDialog(null, messages.getString("messages.customerPhoneNumberAlreadyExists"), messages.getString("window.errorTitle"),
                            JOptionPane.ERROR_MESSAGE);
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
                            JOptionPane.showMessageDialog(null, messages.getString("messages.customerModifiedSuccessfully"), messages.getString("window.editing"),
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    messages.getString("messages.customerModificationfailedID"), messages.getString("window.errorTitle"),
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, messages.getString("messages.customerNoDataChange"), messages.getString("window.alert"),
                            JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, messages.getString("messages.customerNoDataChange"), messages.getString("window.alert"),
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, messages.getString("messages.customerModificationfailed") + ex.getMessage(), messages.getString("window.errorTitle"),
                    JOptionPane.ERROR_MESSAGE);
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

                    return !currentName.equals(newName) || !currentAddress.equals(newAddress)
                            || !currentPhone.equals(newPhone);
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

        JLabel nameLabel = new JLabel(messages.getString("customer.label.name"));
        nameLabel.setBounds(50, 50, 80, 25);
        customerPanel.add(nameLabel);

        JTextField nameText = new JTextField();
        nameText.setBounds(150, 50, 280, 25);
        customerPanel.add(nameText);

        JLabel addressLabel = new JLabel(messages.getString("customer.label.address"));
        addressLabel.setBounds(50, 90, 80, 25);
        customerPanel.add(addressLabel);

        JTextField addressText = new JTextField();
        addressText.setBounds(150, 90, 280, 25);
        customerPanel.add(addressText);

        JLabel phoneLabel = new JLabel(messages.getString("customer.label.phone"));
        phoneLabel.setBounds(50, 130, 100, 25);
        customerPanel.add(phoneLabel);

        JTextField phoneText = new JTextField();
        phoneText.setBounds(150, 130, 280, 25);
        customerPanel.add(phoneText);

        JButton addButton = new JButton(messages.getString("customer.button.add"));
        addButton.setBounds(50, 180, 100, 30);
        customerPanel.add(addButton);

        JButton deleteButton = new JButton(messages.getString("customer.button.delete"));
        deleteButton.setBounds(160, 180, 100, 30);
        customerPanel.add(deleteButton);

        JButton updateButton = new JButton(messages.getString("customer.button.update"));
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
                String input = JOptionPane.showInputDialog(null, messages.getString("messages.inputUserID"),
                        messages.getString("window.deleting"), JOptionPane.QUESTION_MESSAGE);
                if (input != null && !input.isEmpty()) {
                    try {
                        int customerId = Integer.parseInt(input);
                        deleteCustomer(customerId);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, messages.getString("messages.invalidID"), messages.getString("window.errorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, messages.getString("messages.deleteInterupted"), messages.getString("window.alert"),
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Módosítás gomb eseménykezelője
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(null, messages.getString("messages.inputUserID"),
                        messages.getString("window.editing"), JOptionPane.QUESTION_MESSAGE);
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
                            JOptionPane.showMessageDialog(null, messages.getString("messages.customerNoDataChange"), messages.getString("window.alert"),
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, messages.getString("messages.invalidID"), messages.getString("window.errorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, messages.getString("messages.editingInterupted"), messages.getString("window.alert"),
                            JOptionPane.WARNING_MESSAGE);
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

        JLabel ticketNumberLabel = new JLabel(messages.getString("ticket.label.ticketNumber"));
        ticketNumberLabel.setBounds(50, 50, 100, 25);
        ticketPanel.add(ticketNumberLabel);

        JTextField ticketNumberText = new JTextField();
        ticketNumberText.setBounds(150, 50, 280, 25);
        ticketPanel.add(ticketNumberText);

        JLabel issueLabel = new JLabel(messages.getString("ticket.label.issue"));
        issueLabel.setBounds(50, 90, 120, 25);
        ticketPanel.add(issueLabel);

        JTextField issueText = new JTextField();
        issueText.setBounds(150, 90, 280, 25);
        ticketPanel.add(issueText);

        JButton createTicketButton = new JButton(messages.getString("ticket.button.create"));
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
                customerResultArea.append(messages.getString("customer.label.ID") + "\t");
                customerResultArea.append(messages.getString("customer.label.name") + "\t");
                customerResultArea.append(messages.getString("customer.label.address") + "\t");
                customerResultArea.append(messages.getString("customer.label.phone")+ "\t");
                customerResultArea.append("\n");
                while (resultSet.next()) {
                    int id = resultSet.getInt("Vaz");
                    String name = resultSet.getString("Vnev");
                    String address = resultSet.getString("Vcim");
                    String phone = resultSet.getString("Vtelszam");

                    customerResultArea.append(id + "\t");
                    customerResultArea.append(name + "\t");
                    customerResultArea.append(address + "\t");
                    customerResultArea.append(phone + "\t");
                    customerResultArea.append("\n");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LanguageSlector l = new LanguageSlector();
        String folderPath = "src/languages";
        Locale[] locales = l.detectLanguageBundles(folderPath);

        //System.out.println("Detected locales:");
        String[] langs = new String[locales.length];
        int index = 0;
        for (Locale locale : locales) {
            //System.out.println(ResourceBundle.getBundle("languages.messages", locale).getString("file.languageName"));
            langs[(index++)] = ResourceBundle.getBundle("languages.messages", locale).getString("file.languageName");
        }

        SwingUtilities.invokeLater(() -> l.createAndShowGUI(langs,locales));
    }
}

class LanguageSlector {
    public Locale[] detectLanguageBundles(String folderPath) {
        List<Locale> detectedLocales = new ArrayList<>();

        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".properties")) {
                    String fileName = file.getName();
                    Locale locale = extractLocaleFromFileName(fileName);

                    if (locale != null && !detectedLocales.contains(locale)) {
                        detectedLocales.add(locale);
                    }
                }
            }
        }

        return detectedLocales.toArray(new Locale[0]);
    }

    private static String selectedOption; // Variable to store the selected option

    private static Locale extractLocaleFromFileName(String fileName) {
        // Assuming file names are in the format: basename_locale.properties
        String[] parts = fileName.split("_");

        if (parts.length >= 2) {
            // Extract locale from the second part
            String language = parts[1];
            String country = (parts.length > 2) ? parts[2].replace(".properties", "") : "";
            return new Locale(language, country);
        }

        return null;
    }

    void createAndShowGUI(String[] options, Locale[] locales) {
        JFrame frame = new JFrame("Language");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();

        // Create a dropdown (JComboBox)
        JComboBox<String> dropdown = new JComboBox<>(options);

        // Add an ActionListener to handle selection events
        dropdown.addActionListener(e -> {
            // Update the selected option when the dropdown value changes
            selectedOption = (String) dropdown.getSelectedItem();
        });

        // Add the dropdown to the panel
        panel.add(dropdown);

        // Create an OK button
        JButton okButton = new JButton("OK");

        // Add an ActionListener to handle button click
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check if an option is selected before closing the window
                if (selectedOption != null) {
                    SwingUtilities.invokeLater(() -> new AWTDatabase(locales[Arrays.asList(options).indexOf(selectedOption)]));
                    frame.dispose(); // Close the frame
                }
            }
        });

        // Add the OK button to the panel
        panel.add(okButton);

        // Add the panel to the frame
        frame.getContentPane().add(panel);

        // Set frame properties
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);
    }
}