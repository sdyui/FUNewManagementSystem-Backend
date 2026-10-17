package com.funewmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class FuNewManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FuNewManagementSystemApplication.class, args);
        System.out.println(new BCryptPasswordEncoder().encode("admin"));
    }

}
