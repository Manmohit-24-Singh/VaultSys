package com.vaultsys;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {
    private static final String URL = "jdbc:postgresql://127.0.0.1:5432/vaultsys_student";
    private static final String USER = "student";
    private static final String PASSWORD = "student";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
