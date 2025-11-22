package com.vaultsys;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://127.0.0.1:5432/vaultsys_student";
        String user = "student";
        String password = "student";

        System.out.println("Testing connection to: " + url);
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connection successful!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
