/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.start.site;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.initializr.versionresolver.DependencyManagementVersionResolver;
import io.spring.start.site.project.ProjectDescriptionCustomizerConfiguration;
import io.spring.start.site.support.CacheableDependencyManagementVersionResolver;
import io.spring.start.site.support.StartInitializrMetadataUpdateStrategy;
import io.spring.start.site.web.WebConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Initializr website application.
 *
 * @author Stephane Nicoll
 */
@EnableAutoConfiguration
@SpringBootConfiguration
@Import({ ProjectDescriptionCustomizerConfiguration.class, HttpTraceActuatorConfiguration.class, StraceConfig.class })
@EnableCaching
@EnableAsync
public class StartApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartApplication.class, args);
	}

	@Bean
	public WebConfig webConfig() {
		return new WebConfig();
	}

	@Bean
	public StartInitializrMetadataUpdateStrategy initializrMetadataUpdateStrategy(
			RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
		return new StartInitializrMetadataUpdateStrategy(restTemplateBuilder.build(), objectMapper);
	}

	@Bean
	public DependencyManagementVersionResolver dependencyManagementVersionResolver() throws IOException {
		return new CacheableDependencyManagementVersionResolver(DependencyManagementVersionResolver
				.withCacheLocation(Files.createTempDirectory("version-resolver-cache-")));
	}

}
