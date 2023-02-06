package com.xe.fdx.aggregator.external;

import com.xe.fdx.aggregator.model.TrackStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TrackApi {

  private final WebClient client;

  public TrackApi(WebClient.Builder builder) {
    this.client = builder.baseUrl("http://localhost:4000").build();
  }

  public Mono<TrackStatus> getTrackingStatus(String orderNumber) {
    return this.client.get()
        .uri(b -> b.path("/track-status").queryParam("orderNumber", orderNumber).build())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(TrackStatus.class);
  }
}
