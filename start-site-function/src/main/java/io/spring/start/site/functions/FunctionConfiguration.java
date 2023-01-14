package io.spring.start.site.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.initializr.web.controller.ProjectMetadataController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

@SpringBootApplication
public class FunctionConfiguration {

	/*
	 * You need this main method (empty) or explicit <start-class>example.FunctionConfiguration</start-class>
	 * in the POM to ensure boot plug-in makes the correct entry
	 */
	public static void main(String[] args) {
		SpringApplication.run(FunctionConfiguration.class, "--management.endpoints.web.exposure.include=functions");
	}

	@Bean
	public Function<String, String> teste(ApplicationContext applicationContext) {
		return value -> {
			System.out.println(applicationContext.getApplicationName());
			if (value.equals("exception")) {
				throw new RuntimeException("Intentional exception");
			}
			else {
				return value.toUpperCase();
			}
		};
	}

	/*@Bean
	public Function<String, String> test(ProjectMetadataController projectMetadataController, ObjectMapper objectMapper) {
		return value -> {
			try {
				return objectMapper.writeValueAsString(projectMetadataController.serviceCapabilitiesHal());
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		};
	}*/
}
