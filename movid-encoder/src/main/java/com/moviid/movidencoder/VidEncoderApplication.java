package com.moviid.movidencoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@ComponentScan(basePackages = "com.moviid")
@EnableMongoRepositories(basePackages = "com.moviid.proxy.mongodb.repository")
public class VidEncoderApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(VidEncoderApplication.class);
    }
    public static void main(String[] args) {
        SpringApplication.run(VidEncoderApplication.class, args);
    }

}