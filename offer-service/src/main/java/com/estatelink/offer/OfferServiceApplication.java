package com.estatelink.offer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.estatelink.offer", "com.estatelink.common"})
public class OfferServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfferServiceApplication.class, args);
    }
}
