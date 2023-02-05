package io.spring.start.gatling;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class FunctionSimulation extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
    .baseUrl("https://tijpbctkforos3fwk2pgzb3aaq0vtqxl.lambda-url.sa-east-1.on.aws")
    .inferHtmlResources(AllowList(), DenyList(".*\\.js", ".*\\.css", ".*\\.gif", ".*\\.jpeg", ".*\\.jpg", ".*\\.ico", ".*\\.woff", ".*\\.woff2", ".*\\.(t|o)tf", ".*\\.png", ".*detectportal\\.firefox\\.com.*"))
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/109.0");
  
  private Map<CharSequence, String> headers_0 = Map.ofEntries(
    Map.entry("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"),
    Map.entry("If-None-Match", "W/\"f92-1npkuL4L/4CbWl+7W4i4zplx8k0\""),
    Map.entry("Upgrade-Insecure-Requests", "1")
  );
  
  private Map<CharSequence, String> headers_1 = Map.of("Origin", "http://localhost:8080");
  
  private String uri1 = "localhost";

  private ScenarioBuilder scn = scenario("FunctionSimulation")
    .exec(
      http("request_0")
        .get("http://" + uri1 + ":8080/")
        .headers(headers_0)
    )
    .pause(17)
    .exec(
      http("request_1")
        .get("/starter.zip?type=maven-project&language=java&bootVersion=3.0.1&baseDir=demo&groupId=com.example&artifactId=demo&name=demo&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.demo&packaging=jar&javaVersion=17&dependencies=devtools,web,data-jpa")
        .headers(headers_1)
    );

  {
	  setUp(scn.injectOpen(rampUsers(40).during(Duration.ofMinutes(10)))).protocols(httpProtocol);
  }
}
