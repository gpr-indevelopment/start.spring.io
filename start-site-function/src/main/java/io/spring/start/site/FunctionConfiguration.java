package io.spring.start.site;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;

@EnableAutoConfiguration
@SpringBootConfiguration
@Import(ProjectDescriptionCustomizerConfiguration.class)
@EnableCaching
@EnableAsync
public class FunctionConfiguration {

	private static final Log logger = LogFactory.getLog(FunctionConfiguration.class);

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

	@Bean
	public Function<Message<byte[]>, APIGatewayV2HTTPResponse> startSiteFunction(ProjectMetadataController projectMetadataController,
																	DefaultProjectGenerationController projectGenerationController,
																	ObjectMapper objectMapper) {
		return event -> {
			logger.info(String.format("Receiving request for event: %s", event));
			Optional<APIGatewayV2HTTPEvent> httpEvent = parseHttpEvent(event, objectMapper);
			if (httpEvent.isPresent()) {
				return processHttpEvent(httpEvent.get(), projectMetadataController, projectGenerationController, objectMapper);
			}
			return retrieveServiceCapabilities(projectMetadataController);
		};
	}

	private APIGatewayV2HTTPResponse retrieveServiceCapabilities(ProjectMetadataController projectMetadataController) {
		String serviceCapability = projectMetadataController.serviceCapabilitiesHal().getBody();
		logger.info(String.format("Will return service capability: %s", serviceCapability));
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
		response.setStatusCode(200);
		response.setBody(serviceCapability);
		return response;
	}

	private APIGatewayV2HTTPResponse processHttpEvent(APIGatewayV2HTTPEvent event,
													  ProjectMetadataController projectMetadataController,
													  DefaultProjectGenerationController projectGenerationController,
													  ObjectMapper objectMapper) {
		if (event.getRawPath().contains("/starter.zip")) {
			return retrieveStarterZip(event, projectGenerationController, objectMapper);
		}
		return retrieveServiceCapabilities(projectMetadataController);
	}

	private APIGatewayV2HTTPResponse retrieveStarterZip(APIGatewayV2HTTPEvent event,
														DefaultProjectGenerationController projectGenerationController,
														ObjectMapper objectMapper) {
		try {
			ProjectRequest projectRequest = projectGenerationController.projectRequest(event.getHeaders());
			enrichProjectRequest(projectRequest, event, objectMapper);
			logger.info(String.format("Will generate upload file for ProjectRequest: %s", objectMapper.writeValueAsString(projectRequest)));
			return mapFromFileResponseEntity(projectGenerationController.springZip(projectRequest));
		} catch (Exception ex) {
			logger.error("Error found while generating starter zip", ex);
			APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
			response.setBody("Not yet implemented.");
			response.setStatusCode(501);
			return response;
		}
	}

	private void enrichProjectRequest(ProjectRequest projectRequest, APIGatewayV2HTTPEvent event, ObjectMapper objectMapper) {
		List<String> dependencies = extractDependencyList(event);
		WebProjectRequest webProjectRequest = objectMapper.convertValue(event.getQueryStringParameters(), WebProjectRequest.class);
		projectRequest.setDependencies(dependencies);
		projectRequest.setApplicationName(webProjectRequest.getApplicationName());
		projectRequest.setArtifactId(webProjectRequest.getArtifactId());
		projectRequest.setBaseDir(webProjectRequest.getBaseDir());
		projectRequest.setDescription(webProjectRequest.getDescription());
		projectRequest.setBootVersion(webProjectRequest.getBootVersion());
		projectRequest.setGroupId(webProjectRequest.getGroupId());
		projectRequest.setJavaVersion(webProjectRequest.getJavaVersion());
		projectRequest.setLanguage(webProjectRequest.getLanguage());
		projectRequest.setName(webProjectRequest.getName());
		projectRequest.setPackageName(webProjectRequest.getPackageName());
		projectRequest.setPackaging(webProjectRequest.getPackaging());
		projectRequest.setVersion(webProjectRequest.getVersion());
		projectRequest.setType(webProjectRequest.getType());
	}

	private List<String> extractDependencyList(APIGatewayV2HTTPEvent event) {
		List<String> result = new ArrayList<>();
		String dependencies = event.getQueryStringParameters().get("dependencies");
		if (dependencies != null) {
			String[] deps = dependencies.split(",");
			result.addAll(Arrays.asList(deps));
		}
		event.getQueryStringParameters().remove("dependencies");
		return result;
	}

	private Optional<APIGatewayV2HTTPEvent> parseHttpEvent(Message<byte[]> event, ObjectMapper objectMapper) {
		try {
			String eventString = new String(event.getPayload(), StandardCharsets.UTF_8);
			logger.info(String.format("Will try to deserialize UTF-8 payload: %s", eventString));
			return Optional.of(objectMapper.readValue(eventString, APIGatewayV2HTTPEvent.class));
		} catch (Exception ex) {
			logger.warn(String.format("Could not parse event string as HTTP Event: %s", event), ex);
			return Optional.empty();
		}
	}

	private APIGatewayV2HTTPResponse mapFromFileResponseEntity(ResponseEntity<byte[]> responseEntity) {
		APIGatewayV2HTTPResponse result = new APIGatewayV2HTTPResponse();
		result.setIsBase64Encoded(true);
		String base64file = Base64.getEncoder().encodeToString(responseEntity.getBody());
		result.setBody(base64file);
		result.setHeaders(responseEntity.getHeaders().toSingleValueMap());
		result.setStatusCode(200);
		return result;
	}
}
