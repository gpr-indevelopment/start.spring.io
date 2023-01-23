package io.spring.start.site;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.initializr.metadata.DependencyMetadataProvider;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataBuilder;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.metadata.InitializrProperties;
import io.spring.initializr.web.controller.ProjectMetadataController;
import io.spring.initializr.web.support.DefaultInitializrMetadataProvider;
import io.spring.start.site.project.ProjectDescriptionCustomizerConfiguration;
import io.spring.start.site.support.StartInitializrMetadataUpdateStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
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

	/*
	 * You need this main method (empty) or explicit <start-class>example.FunctionConfiguration</start-class>
	 * in the POM to ensure boot plug-in makes the correct entry
	 */
	public static void main(String[] args) {
		SpringApplication.run(FunctionConfiguration.class, args);
	}

	@Bean
	public ProjectMetadataController projectMetadataController(InitializrMetadataProvider metadataProvider,
														DependencyMetadataProvider dependencyMetadataProvider) {
		return new ProjectMetadataController(metadataProvider, dependencyMetadataProvider);
	}

	@Bean
	public StartInitializrMetadataUpdateStrategy initializrMetadataUpdateStrategy(
			RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
		return new StartInitializrMetadataUpdateStrategy(restTemplateBuilder.build(), objectMapper);
	}

	@Bean
	public InitializrMetadataProvider initializrMetadataProvider(InitializrProperties properties,
																 StartInitializrMetadataUpdateStrategy startInitializrMetadataUpdateStrategy) {
		InitializrMetadata metadata = InitializrMetadataBuilder.fromInitializrProperties(properties).build();
		return new DefaultInitializrMetadataProvider(metadata, startInitializrMetadataUpdateStrategy);
	}

	@Bean
	public Function<String, String> test(ProjectMetadataController projectMetadataController) {
		return value -> {
			String serviceCapability = projectMetadataController.serviceCapabilitiesHal().getBody();
			System.out.println(serviceCapability);
			return serviceCapability;
		};
	}
}
