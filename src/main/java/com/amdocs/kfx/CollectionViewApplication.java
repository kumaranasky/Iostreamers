package com.amdocs.kfx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.amdocs.kfx")
public class CollectionViewApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectionViewApplication.class, args);
    }
}
