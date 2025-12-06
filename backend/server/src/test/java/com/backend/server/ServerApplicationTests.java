package com.backend.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.backend.server.config.DatabaseConnection;

@SpringBootTest
class ServerApplicationTests {

    DatabaseConnection database = new DatabaseConnection();

	@Test
	void contextLoads() {
        database.testConnection();
	}

}
