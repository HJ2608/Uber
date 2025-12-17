package com.firstapp.uber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class UberApplication {

	public static void main(String[] args) {
		SpringApplication.run(UberApplication.class, args);
	}

}
