package io.github.gprindevelopment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class LambdaTriggerCron {

    private final FioBandwidthOutputRepository fioBandwidthOutputRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    // Every minute
    @Scheduled(cron = "0 * * * * *")
    public void trigger() throws JsonProcessingException {
        log.debug("Cron triggered. Will call docker-fio lambdas.");
        DockerFioLambda[] lambdas = DockerFioLambda.values();
        for (DockerFioLambda lambda : lambdas) {
            triggerLambda(lambda);
        }
    }

    private void triggerLambda(DockerFioLambda lambda) throws JsonProcessingException {
        log.info("Calling {} lambda", lambda.name());
        ResponseEntity<String> response = restTemplate.getForEntity(lambda.getUrl(), String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Did not receive a 2xx status from docker-fio lambda. Will skip.");
            return;
        }
        FioBandwidthOutput output = parseBandwidthOutput(response.getBody(), lambda);
        normalizeByteUnitToMb(output);
        FioBandwidthOutput saved = fioBandwidthOutputRepository.save(output);
        log.info("Saved FioBandwidthOutput: {}", saved);
    }

    private void normalizeByteUnitToMb(FioBandwidthOutput fioBandwidthOutput) {
        if (fioBandwidthOutput.getByteUnit().equals(ByteUnit.KB.name())) {
            log.info("Will translate KB bandwidth to MB: {} {}", fioBandwidthOutput.getBandwidthValue(), fioBandwidthOutput.getByteUnit());
            fioBandwidthOutput.setByteUnit(ByteUnit.MB.name());
            fioBandwidthOutput.setBandwidthValue(fioBandwidthOutput.getBandwidthValue()/1000);
            log.info("Translated to: {} {}", fioBandwidthOutput.getBandwidthValue(), fioBandwidthOutput.getByteUnit());
        }
    }

    private FioBandwidthOutput parseBandwidthOutput(String fioResponse, DockerFioLambda lambda) throws JsonProcessingException {
        FioRawOutput rawOutput = objectMapper.readValue(fioResponse, FioRawOutput.class);
        String rawDataOutput = new String(rawOutput.getData());
        log.debug("Will parse raw data output: {}", rawDataOutput);
        String bandwidthResult = applyBandwidthRegex(rawDataOutput);
        log.info("Received bandwidth result: {} from lambda: {}", bandwidthResult, lambda.name());
        return buildBandwidthOutputEntity(bandwidthResult, lambda, rawDataOutput);
    }

    private FioBandwidthOutput buildBandwidthOutputEntity(String bandwidthResult, DockerFioLambda lambda, String rawDataOutput) {
        FioBandwidthOutput fioBandwidthOutput = new FioBandwidthOutput();
        fioBandwidthOutput.setTimestamp(System.currentTimeMillis());
        fioBandwidthOutput.setBandwidthValue(applyBandwidthValueRegex(bandwidthResult));
        fioBandwidthOutput.setByteUnit(applyBandwidthUnitRegex(bandwidthResult));
        fioBandwidthOutput.setLambdaName(lambda.name());
        fioBandwidthOutput.setRawOutput(rawDataOutput);
        return fioBandwidthOutput;
    }

    private String applyBandwidthRegex(String rawFioDataOutput) {
        Pattern pattern = Pattern.compile("bw=.*/s");
        Matcher matcher = pattern.matcher(rawFioDataOutput);
        matcher.find();
        return matcher.group();
    }

    private Double applyBandwidthValueRegex(String bandwidthResult) {
        Matcher matcher = Pattern.compile("[\\d\\.]+").matcher(bandwidthResult);
        matcher.find();
        return Double.valueOf(matcher.group());
    }

    private String applyBandwidthUnitRegex(String bandwidthResult) {
        Matcher matcher = Pattern.compile("(\\D+)\\/s").matcher(bandwidthResult);
        matcher.find();
        return matcher.group(1);
    }
}
