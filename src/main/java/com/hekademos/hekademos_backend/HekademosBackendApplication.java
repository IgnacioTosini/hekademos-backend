package com.hekademos.hekademos_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HekademosBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HekademosBackendApplication.class, args);
	}

}
