package com.tolgahan.printerbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PrinterBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrinterBackendApplication.class, args);
	}

}
