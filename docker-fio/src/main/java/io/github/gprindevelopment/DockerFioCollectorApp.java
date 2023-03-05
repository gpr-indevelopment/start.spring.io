package io.github.gprindevelopment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DockerFioCollectorApp {

    public static void main(String[] args) {
        SpringApplication.run(DockerFioCollectorApp.class, args);
    }

}
