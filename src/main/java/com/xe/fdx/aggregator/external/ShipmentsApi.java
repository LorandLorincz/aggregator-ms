package com.xe.fdx.aggregator.external;

import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ShipmentsApi {

  private final WebClient client;

  public ShipmentsApi(WebClient.Builder builder) {
    this.client = builder.baseUrl("http://localhost:4000").build();
  }

  public Mono<List<String>> getShipments(String orderNumber) {
    return this.client.get()
        .uri(b -> b.path("/shipment-products").queryParam("orderNumber", orderNumber).build())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<String>>() {
        });
  }
}
