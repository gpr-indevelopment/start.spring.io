package io.spring.start.gatling.parsing;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Scanner;

public class SimulationParser {

    @Test
    public void parse() throws Exception {
        /*File simulationFile = Paths.get(
                "D:",
                "Mestrado serverless",
                "Simulation",
                "Initializer Function",
                "functionsimulation-20230205003032651",
                "simulation.log"
        ).toFile();*/
        File simulationFile = Paths.get(
                "D:",
                "Mestrado serverless",
                "Simulation",
                "Initializer Server",
                "serversimulation-20230205004533287",
                "simulation.log"
        ).toFile();
        System.out.println("Total runtime of file: " + calculateTotalRuntime(simulationFile));
    }

    private Long calculateTotalRuntime(File simulationFile) throws FileNotFoundException {
        Scanner scanner = new Scanner(simulationFile);
        Long total = 0L;
        while (scanner.hasNextLine()) {
            String currentLine = scanner.nextLine();
            total+=retrieveRuntime(currentLine);
        }
        return total;
    }

    private Long retrieveRuntime(String line) {
        String[] split = line.split("\\s");
        if (!split[0].trim().equals("REQUEST")) {
            return 0L;
        }

        Long startTimestamp = Long.valueOf(split[3]);
        Long endTimestamp = Long.valueOf(split[4]);
        return endTimestamp - startTimestamp;
    }
}
