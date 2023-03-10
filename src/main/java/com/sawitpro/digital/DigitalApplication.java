package com.sawitpro.digital;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition( servers = {
		@Server(url = "/", description = "Default Server URL")},
		info = @Info(title = "Digital Sawitpro Service API",
				version = "v1.0.0",
				license = @License(name = "Sawitpro License")))
public class DigitalApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigitalApplication.class, args);
	}

}
