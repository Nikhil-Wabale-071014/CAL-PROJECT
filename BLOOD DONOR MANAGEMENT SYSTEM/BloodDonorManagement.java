import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class BloodDonorManagement extends JFrame {
    private List<Donor> donors = new ArrayList<>();
    private JTable donorTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> bloodGroupFilter;
    private JComboBox<String> cityFilter;

    public BloodDonorManagement() {
        setTitle("❤️ Blood Donor Management System");
        setSize(950, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // Set global font and colors
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 13));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabs.addTab("📋 Donor List", createDonorListPanel());
        tabs.addTab("➕ Add Donor", createAddDonorPanel());

        add(tabs);
    }

    private JPanel createDonorListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Age", "Blood Group", "Phone", "City", "Last Donation"}, 0);
        donorTable = new JTable(tableModel);
        donorTable.setRowHeight(25);
        donorTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(donorTable);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(10);
        bloodGroupFilter = new JComboBox<>(new String[]{"All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        cityFilter = new JComboBox<>(new String[]{"All", "Mumbai", "Delhi", "Chennai", "Kolkata", "Other"});

        topPanel.add(new JLabel("🔍 Search Name:"));
        topPanel.add(searchField);
        topPanel.add(new JLabel("🩸 Blood Group:"));
        topPanel.add(bloodGroupFilter);
        topPanel.add(new JLabel("📍 City:"));
        topPanel.add(cityFilter);

        JPanel bottomPanel = new JPanel();
        JButton deleteButton = new JButton("🗑️ Delete Donor");
        JButton exportButton = new JButton("📁 Export List");

        deleteButton.setBackground(new Color(255, 100, 100));
        exportButton.setBackground(new Color(100, 200, 255));

        bottomPanel.add(deleteButton);
        bottomPanel.add(exportButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                updateTable();
            }
        });
        bloodGroupFilter.addActionListener(e -> updateTable());
        cityFilter.addActionListener(e -> updateTable());

        deleteButton.addActionListener(e -> deleteDonor());
        exportButton.addActionListener(e -> exportToFile());

        return panel;
    }

    private JPanel createAddDonorPanel() {
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 100, 20, 100));

        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JComboBox<String> bloodGroupField = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        JTextField phoneField = new JTextField();
        JComboBox<String> cityField = new JComboBox<>(new String[]{"Mumbai", "Delhi", "Chennai", "Kolkata", "Other"});

        // Date Picker using Spinner
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd-MM-yyyy"));

        JButton submitButton = new JButton("✅ Submit Donor");
        submitButton.setBackground(new Color(120, 220, 120));

        formPanel.add(new JLabel("Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Age:")); formPanel.add(ageField);
        formPanel.add(new JLabel("Blood Group:")); formPanel.add(bloodGroupField);
        formPanel.add(new JLabel("Phone:")); formPanel.add(phoneField);
        formPanel.add(new JLabel("City:")); formPanel.add(cityField);
        formPanel.add(new JLabel("Last Donation Date:")); formPanel.add(dateSpinner);
        formPanel.add(new JLabel("")); formPanel.add(submitButton);

        submitButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                int age = Integer.parseInt(ageField.getText());
                String bloodGroup = bloodGroupField.getSelectedItem().toString();
                String phone = phoneField.getText();
                String city = cityField.getSelectedItem().toString();
                Date lastDonation = (Date) dateSpinner.getValue();

                Donor donor = new Donor(donors.size() + 1, name, age, bloodGroup, phone, city, lastDonation);
                donors.add(donor);

                JOptionPane.showMessageDialog(this, "🎉 Donor Added Successfully!");
                nameField.setText(""); ageField.setText(""); phoneField.setText("");
                updateTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Invalid input. Please check your values.");
            }
        });

        return formPanel;
    }

    private void deleteDonor() {
        int selectedRow = donorTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            donors.removeIf(d -> d.getId() == id);
            updateTable();
        } else {
            JOptionPane.showMessageDialog(this, "⚠️ Please select a donor to delete.");
        }
    }

    private void exportToFile() {
        try (var writer = new java.io.FileWriter("donors.txt")) {
            for (Donor d : donors) {
                writer.write(d.toString() + "\n");
            }
            JOptionPane.showMessageDialog(this, "✅ Donor list exported to donors.txt");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Failed to export: " + e.getMessage());
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        String nameFilter = searchField.getText().toLowerCase();
        String bloodFilter = bloodGroupFilter.getSelectedItem().toString();
        String cityFilterVal = cityFilter.getSelectedItem().toString();

        for (Donor d : donors) {
            boolean matches = d.getName().toLowerCase().contains(nameFilter);
            if (!bloodFilter.equals("All")) matches &= d.getBloodGroup().equals(bloodFilter);
            if (!cityFilterVal.equals("All")) matches &= d.getCity().equals(cityFilterVal);

            if (matches) {
                tableModel.addRow(new Object[]{
                        d.getId(), d.getName(), d.getAge(), d.getBloodGroup(), d.getPhone(),
                        d.getCity(), new SimpleDateFormat("dd-MM-yyyy").format(d.getLastDonation())
                });
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BloodDonorManagement().setVisible(true));
    }

    // Donor class
    class Donor {
        private int id;
        private String name;
        private int age;
        private String bloodGroup;
        private String phone;
        private String city;
        private Date lastDonation;

        public Donor(int id, String name, int age, String bloodGroup, String phone, String city, Date lastDonation) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.bloodGroup = bloodGroup;
            this.phone = phone;
            this.city = city;
            this.lastDonation = lastDonation;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getBloodGroup() { return bloodGroup; }
        public String getPhone() { return phone; }
        public String getCity() { return city; }
        public Date getLastDonation() { return lastDonation; }

        @Override
        public String toString() {
            return id + ". " + name + " | " + bloodGroup + " | " + phone + " | " + city +
                    " | Last Donation: " + new SimpleDateFormat("dd-MM-yyyy").format(lastDonation);
        }
    }
}
