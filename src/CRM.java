import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.Vector;

public class CRM extends JFrame {
    private static final String url = "jdbc:mysql://localhost:3306/crmdb";
    private static final String user = "root";
    private static final String password = "";
    private JComboBox<String> tableComboBox;
    private JTable dataTable;
    private JButton insertButton;
    private JButton modifyButton;
    private JTextField searchField;
    private JComboBox<String> searchColumnComboBox;
    private LanguageBundle LanguageBundle;

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private ResultSetMetaData metaData;
    private int numberOfColumns;

    public CRM() {
        super("Database Viewer");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel databaseViewerPanel = new JPanel(new BorderLayout());
        tableComboBox = new JComboBox<>();
        dataTable = new JTable();
        insertButton = new JButton("Beszúrás");
        modifyButton = new JButton("Módosítás");
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, searchField.getPreferredSize().height));
        searchColumnComboBox = new JComboBox<>();

        Dimension buttonSize = new Dimension(150, 30);
        insertButton.setPreferredSize(buttonSize);
        modifyButton.setPreferredSize(buttonSize);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(insertButton);
        buttonPanel.add(modifyButton);
        buttonPanel.add(new JLabel("Keresés: "));
        buttonPanel.add(searchColumnComboBox);
        buttonPanel.add(searchField);

        JComboBox<String> languageComboBox = new JComboBox<>(new String[]{"Magyar", "English", "中国人", "Deutsch", "日本", "Français", "Русский"});

        languageComboBox.addActionListener(e -> changeLanguage((String) languageComboBox.getSelectedItem()));

        // Panel for holding table and language selection
        JPanel selectionPanel = new JPanel(new FlowLayout());
        selectionPanel.add(new JLabel("Nyelv: "));
        selectionPanel.add(languageComboBox);
        selectionPanel.add(new JLabel("Tábla: "));
        selectionPanel.add(tableComboBox);

        initializeDatabase();
        initializeSearchColumns();

        try {
            DatabaseMetaData dbMetaData = connection.getMetaData();
            ResultSet tables = dbMetaData.getTables("crmdb", null, null, new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableComboBox.addItem(tableName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableComboBox.addActionListener(e -> loadTableData((String) tableComboBox.getSelectedItem()));
        loadTableData((String) tableComboBox.getSelectedItem()); 

        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editRecord();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    deleteRecord();
                }
            }
        });

        insertButton.addActionListener(e -> insertRecord());
        modifyButton.addActionListener(e -> modifyRecord()); 
        searchField.addActionListener(e -> performSearch());

        DefaultTableModel tableModel = (DefaultTableModel) dataTable.getModel();
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (row >= 0 && column >= 0) {
                    saveChanges(row, column);
                }
            }
        });

        databaseViewerPanel.setLayout(new BorderLayout());
        databaseViewerPanel.add(selectionPanel, BorderLayout.NORTH);
        databaseViewerPanel.add(new JScrollPane(dataTable), BorderLayout.CENTER);
        databaseViewerPanel.add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Adattábla kezelő", null, databaseViewerPanel, "View and manage database records");
        JPanel ticketPanel = new JPanel(new BorderLayout());

        tabbedPane.addTab("Ticketek kezelése", null, ticketPanel, "Manage tickets");

        add(tabbedPane);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }   

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(true);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    
            DatabaseMetaData dbMetaData = connection.getMetaData();
            ResultSet tables = dbMetaData.getTables("crmdb", null, null, new String[]{"TABLE"});
    
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableComboBox.addItem(tableName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTableData(String tableName) {
        try {
            resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            metaData = resultSet.getMetaData();
    
            numberOfColumns = metaData.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= numberOfColumns; i++) {
                columnNames.add(metaData.getColumnName(i));
            }
    
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };
    
            while (resultSet.next()) {
                Vector<Object> rowData = new Vector<>();
                for (int i = 1; i <= numberOfColumns; i++) {
                    rowData.add(resultSet.getObject(i));
                }
                tableModel.addRow(rowData);
            }
    
            dataTable.setModel(tableModel);
            resultSet = statement.executeQuery("SELECT * FROM " + tableName);

            initializeSearchColumns();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editRecord() {
        int selectedRow = dataTable.getSelectedRow();
        int selectedColumn = dataTable.getSelectedColumn();
        if (selectedRow == -1 || selectedColumn == -1) {
            JOptionPane.showMessageDialog(this, "Nincs kiválasztott rekord vagy oszlop.", "Hiba",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        saveChanges(selectedRow, selectedColumn);
    }

    private void deleteRecord() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Nincs kiválasztott rekord.", "Hiba", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this, "Biztosan törölni szeretné a kiválasztott rekordot?",
                "Törlés megerősítése", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try {
                resultSet.absolute(selectedRow + 1);
                resultSet.deleteRow();
                loadTableData((String) tableComboBox.getSelectedItem());
                JOptionPane.showMessageDialog(this, "Rekord sikeresen törölve.");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Hiba történt a rekord törlése során.", "Hiba",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void insertRecord() {
        String selectedTable = (String) tableComboBox.getSelectedItem();
        try {
            loadTableData(selectedTable);
            ResultSetMetaData tableMetaData = resultSet.getMetaData();
            int columnCount = tableMetaData.getColumnCount();

            JPanel panel = new JPanel(new GridLayout(0, 2));
            JTextField[] textFields = new JTextField[columnCount];

            for (int i = 1; i <= columnCount; i++) {
                String columnName = tableMetaData.getColumnName(i);
                panel.add(new JLabel(columnName + ":"));
                textFields[i - 1] = new JTextField();
                panel.add(textFields[i - 1]);
            }

            int result = JOptionPane.showConfirmDialog(null, panel, "Adatok megadása", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + selectedTable + " (");
                    for (int i = 1; i <= columnCount; i++) {
                        queryBuilder.append(tableMetaData.getColumnName(i));
                        if (i < columnCount) {
                            queryBuilder.append(", ");
                        }
                    }
                    queryBuilder.append(") VALUES (");
                    for (int i = 1; i <= columnCount; i++) {
                        queryBuilder.append("?");
                        if (i < columnCount) {
                            queryBuilder.append(", ");
                        }
                    }
                    queryBuilder.append(")");

                    PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString());

                    for (int i = 1; i <= columnCount; i++) {
                        preparedStatement.setString(i, textFields[i - 1].getText());
                    }

                    preparedStatement.executeUpdate();

                    loadTableData(selectedTable);

                    JOptionPane.showMessageDialog(this, "Rekord sikeresen beszúrva.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Hiba történt a rekord beszúrása során.", "Hiba",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Hiba történt az oszlopok lekérdezése során.", "Hiba",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifyRecord() {
        int selectedRow = dataTable.getSelectedRow();
        int selectedColumn = dataTable.getSelectedColumn();
        if (selectedRow == -1 || selectedColumn == -1) {
            JOptionPane.showMessageDialog(this, "Nincs kiválasztott rekord vagy oszlop.", "Hiba",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        saveChanges(selectedRow, selectedColumn);
    }

    private void saveChanges(int row, int column) {
        try {
            Object newValue = dataTable.getValueAt(row, column);
            resultSet.absolute(row + 1);
            resultSet.updateObject(column + 1, newValue);
            resultSet.updateRow();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            loadTableData((String) tableComboBox.getSelectedItem());
            JOptionPane.showMessageDialog(this, "Rekord sikeresen módosítva az adatbázisban.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Hiba történt a rekord módosítása során.", "Hiba",
                    JOptionPane.ERROR_MESSAGE);
        }
    }    
    
    private void updateRowInDatabase() throws SQLException {
        resultSet.updateRow();
        if (!connection.getAutoCommit()) {
            connection.commit();
        }
    }       

    private void initializeSearchColumns() {
        String[] columns = getColumnNames();
        for (String column : columns) {
            searchColumnComboBox.addItem(column);
        }
    }
    
    private String[] getColumnNames() {
        int selectedTableIndex = tableComboBox.getSelectedIndex();
        String tableName = tableComboBox.getItemAt(selectedTableIndex);
        Vector<String> columnNames = new Vector<>();
    
        try {
            resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData tableMetaData = resultSet.getMetaData();
            int columnCount = tableMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(tableMetaData.getColumnName(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return columnNames.toArray(new String[0]);
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText();
        String selectedColumn = (String) searchColumnComboBox.getSelectedItem();
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(dataTable.getModel());
        dataTable.setRowSorter(rowSorter);
    
        if (searchTerm.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            int columnIndex = getColumnIndex(selectedColumn);
            if (columnIndex != -1) {
                rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchTerm, columnIndex));
            } else {
                System.err.println("Invalid column name: " + selectedColumn);
            }
        }
    }
    
    private int getColumnIndex(String columnName) {
        try {
            ResultSetMetaData tableMetaData = resultSet.getMetaData();
            int columnCount = tableMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                if (columnName.equals(tableMetaData.getColumnName(i))) {
                    return i - 1; 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CRM());
    }

    private void changeLanguage(String selectedLanguage) {
        LanguageBundle = new LanguageBundle(selectedLanguage);

        // Gombok és egyéb feliratok lefordítása
        insertButton.setText(LanguageBundle.getString("insert_button"));
        modifyButton.setText(LanguageBundle.getString("modify_button"));
        searchField.setToolTipText(LanguageBundle.getString("search_placeholder"));
        // További fordítások itt...

        // Hibaüzenetek beállítása
        String[] errorMessages = {
                "error_no_record_selected",
                "error_delete_confirm",
                "error_delete_record",
                "error_insert_record",
                "error_modify_record",
                "error_fetch_columns",
                "error_invalid_column"
        };

        for (String errorMessage : errorMessages) {
            UIManager.put(errorMessage, LanguageBundle.getString(errorMessage));
        }
    }
}