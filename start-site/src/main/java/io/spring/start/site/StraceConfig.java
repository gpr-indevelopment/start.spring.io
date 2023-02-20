package io.spring.start.site;

import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StraceConfig {

	private static final Log logger = LogFactory.getLog(StraceConfig.class);

	@PostConstruct
	public void postConstruct() {
		try {
			ProcessBuilder pb = new ProcessBuilder("/usr/app/strace.sh");
			Process p = pb.start();
			logger.info("Successfully started strace process.");
		}
		catch (Exception e) {
			logger.error("Not possible to start strace process", e);
		}
	}

}
