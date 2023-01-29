package io.spring.start.site;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.initializr.metadata.*;
import io.spring.initializr.versionresolver.DependencyManagementVersionResolver;
import io.spring.initializr.web.controller.DefaultProjectGenerationController;
import io.spring.initializr.web.controller.ProjectGenerationController;
import io.spring.initializr.web.controller.ProjectMetadataController;
import io.spring.initializr.web.project.*;
import io.spring.initializr.web.support.DefaultInitializrMetadataProvider;
import io.spring.start.site.project.ProjectDescriptionCustomizerConfiguration;
import io.spring.start.site.support.CacheableDependencyManagementVersionResolver;
import io.spring.start.site.support.StartInitializrMetadataUpdateStrategy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.nio.file.Files;

@EnableAutoConfiguration
@SpringBootConfiguration
@Import(ProjectDescriptionCustomizerConfiguration.class)
@EnableCaching
@EnableAsync
public class FunctionApplication {

	/*
	 * You need this main method (empty) or explicit <start-class>example.FunctionConfiguration</start-class>
	 * in the POM to ensure boot plug-in makes the correct entry
	 */

	/*public static void main(String[] args) {
		SpringApplication.run(FunctionApplication.class, args);
	}*/

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
	public ProjectGenerationController<ProjectRequest> projectGenerationController(
			InitializrMetadataProvider metadataProvider,
			ObjectProvider<ProjectRequestPlatformVersionTransformer> platformVersionTransformer,
			ApplicationContext applicationContext) {
		ProjectGenerationInvoker<ProjectRequest> projectGenerationInvoker = new ProjectGenerationInvoker<>(
				applicationContext, new DefaultProjectRequestToDescriptionConverter(platformVersionTransformer
				.getIfAvailable(DefaultProjectRequestPlatformVersionTransformer::new)));
		return new DefaultProjectGenerationController(metadataProvider, projectGenerationInvoker);
	}

	@Bean
	public DependencyManagementVersionResolver dependencyManagementVersionResolver() throws IOException {
		return new CacheableDependencyManagementVersionResolver(DependencyManagementVersionResolver
				.withCacheLocation(Files.createTempDirectory("version-resolver-cache-")));
	}
}
