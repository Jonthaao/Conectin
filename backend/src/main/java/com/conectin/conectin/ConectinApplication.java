package com.conectin.conectin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ConectinApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConectinApplication.class, args);
	}

}
