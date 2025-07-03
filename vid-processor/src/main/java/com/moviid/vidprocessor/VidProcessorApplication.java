package com.moviid.vidprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.logging.Logger;

@EnableAsync
@SpringBootApplication
@ComponentScan({"com.moviid"})
@EnableMongoRepositories(basePackages = "com.moviid.proxy.mongodb.repository")
public class VidProcessorApplication extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(VidProcessorApplication.class);
	}
	public static void main(String[] args) {
		SpringApplication.run(VidProcessorApplication.class, args);
	}

}