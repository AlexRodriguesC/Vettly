package com.vettly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VettlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(VettlyApplication.class, args);
    }
}
