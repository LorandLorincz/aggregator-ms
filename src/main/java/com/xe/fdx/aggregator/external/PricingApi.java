package com.xe.fdx.aggregator.external;

import java.math.BigDecimal;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PricingApi {

  private final WebClient client;

  public PricingApi(WebClient.Builder builder) {
    this.client = builder.baseUrl("http://localhost:4000").build();
  }

  public Mono<BigDecimal> getPricing(String countryCode) {
    return this.client.get()
        .uri(b -> b.path("/pricing").queryParam("countryCode", countryCode).build())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(BigDecimal.class);
  }
}
