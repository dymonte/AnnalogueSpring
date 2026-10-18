package com.annalogue.m2.steps;

import io.cucumber.spring.CucumberContextConfiguration;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(CucumberSpringConfiguration.NoMailConfiguration.class)
public class CucumberSpringConfiguration {

    @TestConfiguration
    static class NoMailConfiguration {

        @Bean
        @Primary
        JavaMailSender noMailSender() {
            return Mockito.mock(JavaMailSender.class);
        }
    }
}