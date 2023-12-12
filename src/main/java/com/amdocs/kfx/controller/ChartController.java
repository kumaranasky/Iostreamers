package com.amdocs.kfx.controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Controller
public class ChartController implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChartController.class);
    private static final String DATABASE_PROPERTIES_FILE = "database.properties";

   private Connection connection;


    @PostConstruct
    private void initializeDatabaseConnection() {
        try {
            Properties props = loadProperties(DATABASE_PROPERTIES_FILE);

            String host = props.getProperty("db.host");
            String port = props.getProperty("db.port");
            String sid = props.getProperty("db.sid");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            String databaseURL = String.format("jdbc:oracle:thin:@%s:%s:%s", host, port, sid);

            connection = DriverManager.getConnection(databaseURL, username, password);
        } catch (IOException | SQLException e) {
            handleException(e);
        }
    }

    private Properties loadProperties(String fileName) throws IOException {
        Properties props = new Properties();
        try (InputStream input = ChartController.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new IOException("Unable to find " + fileName + " file.");
            }
            props.load(input);
        }
        return props;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<String> categories = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        try {
            String sqlQuery = "SELECT ID_ISSUE, TOT FROM AHSS_MES_REP";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    categories.add(resultSet.getString("ID_ISSUE"));
                    values.add(resultSet.getInt("TOT"));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
            model.addAttribute("error", "An error occurred while fetching data from the database.");
            return "error";
        }

        model.addAttribute("categories", categories);
        model.addAttribute("values", values);

        return "index";
    }

    private void handleSQLException(SQLException e) {
        LOGGER.error("SQL Exception occurred. SQL State: {}, Error Code: {}", e.getSQLState(), e.getErrorCode(), e);
    }

    private void handleException(Exception e) {
        LOGGER.error("Exception occurred: {}", e.getMessage(), e);
    }

    @PreDestroy
    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
