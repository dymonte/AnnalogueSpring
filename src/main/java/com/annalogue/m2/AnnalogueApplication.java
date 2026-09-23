package com.annalogue.m2;

import com.annalogue.m2.repositories.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@SpringBootApplication
public class AnnalogueApplication {

    static void main(String[] args) {
		SpringApplication.run(AnnalogueApplication.class, args);
	}
}
