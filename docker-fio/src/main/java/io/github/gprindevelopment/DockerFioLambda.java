package io.github.gprindevelopment;

import lombok.Getter;

@Getter
public enum DockerFioLambda {
    SA_EAST_1("https://g4o3qkj4bnsnhjghrak4xijuuu0aqytg.lambda-url.sa-east-1.on.aws/"),
    US_EAST_1("https://lkxqxjcgbykyrryqsqhf5rp6li0caxyg.lambda-url.us-east-1.on.aws/");

    private final String url;

    DockerFioLambda(String url) {
        this.url = url;
    }
}
