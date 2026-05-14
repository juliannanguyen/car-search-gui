import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Hashtable;

public class CarSearchGUI extends JFrame {
    private JTextField inputField;
    private JTextArea resultArea;
    private JSlider mpgSlider;
    private JSlider horsepowerSlider;

    // Factor for scaling to allow decimal values
    private static final int SCALE_FACTOR = 10;

    public CarSearchGUI() {
        // Initialize JFrame
        setTitle("Car Search");
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel for user inputs
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create input text box and search button
        inputField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            String userInput = inputField.getText().trim();
            displayResults(userInput);
        });

        // Create MPG slider
        mpgSlider = new JSlider(JSlider.HORIZONTAL, 0, 500, 0);
        mpgSlider.setMajorTickSpacing(100); // represents 10.0 increments
        mpgSlider.setMinorTickSpacing(10);  // represents 1.0 increments
        mpgSlider.setPaintTicks(true);
        mpgSlider.setPaintLabels(true);
        JLabel mpgLabel = new JLabel("MPG: 0.0");

        // Create and set MPG label table
        Hashtable<Integer, JLabel> mpgLabels = new Hashtable<>();
        for (int i = 0; i <= 50; i += 10) {
            mpgLabels.put(i * SCALE_FACTOR, new JLabel(String.format("%.1f", (double) i)));
        }
        mpgSlider.setLabelTable(mpgLabels);

        // Update label when slider value changes
        mpgSlider.addChangeListener(e -> {
            double value = (double) mpgSlider.getValue() / SCALE_FACTOR;
            mpgLabel.setText(String.format("MPG: %.1f", value));
        });

        // Create horsepower slider
        horsepowerSlider = new JSlider(JSlider.HORIZONTAL, 0, 3000, 0);
        horsepowerSlider.setMajorTickSpacing(1000); // represents 100.0 increments
        horsepowerSlider.setMinorTickSpacing(100);  // represents 10.0 increments
        horsepowerSlider.setPaintTicks(true);
        horsepowerSlider.setPaintLabels(true);
        JLabel horsepowerLabel = new JLabel("Horsepower: 0.0");

        // Create and set Horsepower label table
        Hashtable<Integer, JLabel> horsepowerLabels = new Hashtable<>();
        for (int i = 0; i <= 300; i += 100) {
            horsepowerLabels.put(i * SCALE_FACTOR, new JLabel(String.format("%.1f", (double) i)));
        }
        horsepowerSlider.setLabelTable(horsepowerLabels);

        // Update label when slider value changes
        horsepowerSlider.addChangeListener(e -> {
            double value = (double) horsepowerSlider.getValue() / SCALE_FACTOR;
            horsepowerLabel.setText(String.format("Horsepower: %.1f", value));
        });

        // Create result text area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Add components to input panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Enter search query (ALL for all records):"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        inputPanel.add(inputField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Select MPG:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        inputPanel.add(mpgSlider, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        inputPanel.add(mpgLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Select Horsepower:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        inputPanel.add(horsepowerSlider, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        inputPanel.add(horsepowerLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        inputPanel.add(searchButton, gbc);

        // Add components to JFrame
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Display JFrame
        setVisible(true);
    }

    private void displayResults(String userInput) {
        // Clear previous results
        resultArea.setText("");

        // Check if input is empty
        if (userInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search query.", "No Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Connect to the database and execute corresponding SQL statements based on user input
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/Auto", "testuser2", "Pa$$word");
            Statement statement = connection.createStatement();

            String query = "SELECT * FROM Auto";
            boolean hasConditions = false;

            if (!userInput.equals("ALL")) {
                query += " WHERE CarName LIKE '%" + userInput + "%'";
                hasConditions = true;
            }

            double selectedMPG = (double) mpgSlider.getValue() / SCALE_FACTOR;
            double selectedHorsepower = (double) horsepowerSlider.getValue() / SCALE_FACTOR;

            if (selectedMPG > 0 && selectedHorsepower > 0) {
                if (hasConditions) {
                    query += " AND";
                } else {
                    query += " WHERE";
                    hasConditions = true;
                }
                query += " MPG = " + selectedMPG + " AND Horsepower = " + selectedHorsepower;
            } else if (selectedMPG > 0) {
                if (hasConditions) {
                    query += " AND";
                } else {
                    query += " WHERE";
                    hasConditions = true;
                }
                query += " MPG = " + selectedMPG;
            } else if (selectedHorsepower > 0) {
                if (hasConditions) {
                    query += " AND";
                } else {
                    query += " WHERE";
                    hasConditions = true;
                }
                query += " Horsepower = " + selectedHorsepower;
            }

            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                resultArea.append("MPG: " + resultSet.getString("MPG") + "\n" +
                        "Cylinders: " + resultSet.getString("Cylinders") + "\n" +
                        "Displacement: " + resultSet.getString("Displacement") + "\n" +
                        "Horsepower: " + resultSet.getString("Horsepower") + "\n" +
                        "Weight: " + resultSet.getString("Weight") + "\n" +
                        "Acceleration: " + resultSet.getString("Acceleration") + "\n" +
                        "ModelYear: " + resultSet.getString("ModelYear") + "\n" +
                        "Origin: " + resultSet.getString("Origin") + "\n" +
                        "CarName: " + resultSet.getString("CarName") + "\n\n");
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CarSearchGUI();
            }
        });
    }
}
