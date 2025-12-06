package com.backend.server.config;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseConnection {
    
    private DataSource dataSource;
    @Autowired
    public void testConnection () {
        try(Connection conn = dataSource.getConnection()){
            System.out.println("Database is connected" + conn.getMetaData().getDatabaseProductName());
        }catch(Exception error) {
            System.out.print(error);
        }
    }
}
