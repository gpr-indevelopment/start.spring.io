package io.spring.start.site;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class StraceConfig {

	@PostConstruct
	public void postConstruct() throws IOException {
		ProcessBuilder pb = new ProcessBuilder("/usr/app/strace.sh");
		Process p = pb.start();
	}

}
