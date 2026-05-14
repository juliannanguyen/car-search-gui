import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class SimpleJdbc {
    public static void main(String[] args) {
        String filePath = "/Users/juliannanguyen/IdeaProjects/Project Database/src/auto_mpg.txt";

        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded");

            // Establish connection to the database
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/Auto", "testuser2", "Pa$$word")) {
                System.out.println("Database connected");

                // Clear the table
                String clearTableQuery = "DELETE FROM Auto";
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate(clearTableQuery);
                    System.out.println("Table cleared.");
                }

                String insertQuery = "INSERT INTO Auto (MPG, Cylinders, Displacement, Horsepower, Weight, Acceleration, ModelYear, Origin, CarName) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
                     BufferedReader br = new BufferedReader(new FileReader(filePath))) {

                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split("\\s+");

                        // Ensure we have the expected number of parts
                        if (parts.length >= 9) {
                            String mpg = parts[0].equals("NA") ? null : parts[0];
                            String cylinders = parts[1].equals("NA") ? null : parts[1];
                            String displacement = parts[2].equals("NA") ? null : parts[2];
                            String horsepower = parts[3].equals("NA") ? null : parts[3];
                            String weight = parts[4].equals("NA") ? null : parts[4];
                            String acceleration = parts[5].equals("NA") ? null : parts[5];
                            String modelYear = parts[6].equals("NA") ? null : parts[6];
                            String origin = parts[7].equals("NA") ? null : parts[7];

                            // Combine parts for Car Name removing quotes
                            StringBuilder carNameBuilder = new StringBuilder();
                            for (int i = 8; i < parts.length; i++) {
                                carNameBuilder.append(parts[i]).append(" ");
                            }
                            String carName = carNameBuilder.toString().trim().replace("\"", "");

                            preparedStatement.setString(1, mpg);
                            preparedStatement.setString(2, cylinders);
                            preparedStatement.setString(3, displacement);
                            preparedStatement.setString(4, horsepower);
                            preparedStatement.setString(5, weight);
                            preparedStatement.setString(6, acceleration);
                            preparedStatement.setString(7, modelYear);
                            preparedStatement.setString(8, origin);
                            preparedStatement.setString(9, carName);

                            try {
                                preparedStatement.executeUpdate();
                            } catch (SQLIntegrityConstraintViolationException e) {
                                System.err.println("Duplicate entry found for: " + carName + " (" + modelYear + "), skipping.");
                            }
                        } else {
                            System.err.println("Skipping malformed line: " + line);
                        }
                    }

                    System.out.println("Data inserted successfully.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
