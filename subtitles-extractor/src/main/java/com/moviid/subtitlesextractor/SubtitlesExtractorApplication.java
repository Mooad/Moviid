package com.moviid.subtitlesextractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.moviid")
@EnableMongoRepositories(basePackages = "com.moviid.proxy.mongodb.repository")
public class SubtitlesExtractorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubtitlesExtractorApplication.class, args);
    }
}
