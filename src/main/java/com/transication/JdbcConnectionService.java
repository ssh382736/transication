package com.transication;


import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;

@Service
public class JdbcConnectionService {

    private static String driver = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/transicationtest";
    private static String username = "root";
    private static String password = "root";

    public static Connection getConnection(){
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url,username,password);

        } catch (Exception e) {

        }
        return null;
    }
}
