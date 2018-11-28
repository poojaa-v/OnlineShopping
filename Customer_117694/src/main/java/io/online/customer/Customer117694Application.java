package io.online.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("io.online.customer")
public class Customer117694Application {

	public static void main(String[] args) {
		SpringApplication.run(Customer117694Application.class, args);
	}
}
