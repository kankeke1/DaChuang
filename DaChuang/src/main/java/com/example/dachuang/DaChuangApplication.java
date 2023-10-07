package com.example.dachuang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DaChuangApplication {

	public static void main(String[] args) {
		SpringApplication.run(DaChuangApplication.class, args);
	}

}
