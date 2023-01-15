package io.spring.start.site;

import io.spring.initializr.metadata.DependencyMetadataProvider;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.web.controller.ProjectMetadataController;
import io.spring.start.site.project.ProjectDescriptionCustomizerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.function.Function;

@EnableAutoConfiguration
@SpringBootConfiguration
@Import(ProjectDescriptionCustomizerConfiguration.class)
@EnableCaching
@EnableAsync
public class FunctionConfiguration {

	@Autowired
	private InitializrMetadataProvider initializrMetadataProvider;

	@Autowired
	private DependencyMetadataProvider dependencyMetadataProvider;

	/*
	 * You need this main method (empty) or explicit <start-class>example.FunctionConfiguration</start-class>
	 * in the POM to ensure boot plug-in makes the correct entry
	 */
	public static void main(String[] args) {
		SpringApplication.run(FunctionConfiguration.class, args);
	}

	/*@Bean
	public Function<String, String> test() {
		return value -> {
			if (value.equals("exception")) {
				throw new RuntimeException("Intentional exception");
			}
			else {
				return value.toUpperCase();
			}
		};
	}*/

	@Bean
	public Function<String, String> test() {
		return value -> {
			ProjectMetadataController projectMetadataController = new ProjectMetadataController(initializrMetadataProvider, dependencyMetadataProvider);
			System.out.println("Chegou no corpo da função");
			String serviceCapability = projectMetadataController.serviceCapabilitiesHal().getBody();
			System.out.println(serviceCapability);
			return serviceCapability;
		};
	}
}
