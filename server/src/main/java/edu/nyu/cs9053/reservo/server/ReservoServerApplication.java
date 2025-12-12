package edu.nyu.cs9053.reservo.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReservoServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReservoServerApplication.class, args);
        // Port is configured in application.properties (default: 8081)
        String port = System.getProperty("server.port", "8081");
        System.out.println("Reservo Server started on http://localhost:" + port);
        System.out.println("H2 Console available at http://localhost:" + port + "/h2-console");
    }
}

