package com.canchas.reservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.canchas.reservas")

public class ReservasApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReservasApplication.class, args);
	}
}