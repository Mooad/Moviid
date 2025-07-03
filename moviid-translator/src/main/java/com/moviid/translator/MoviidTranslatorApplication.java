package com.moviid.translator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;

@SpringBootApplication
@EnableSqs
public class MoviidTranslatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoviidTranslatorApplication.class, args);
    }

}
