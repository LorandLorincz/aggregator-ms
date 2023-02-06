package com.xe.fdx.aggregator.business;

import com.xe.fdx.aggregator.external.PricingApi;
import com.xe.fdx.aggregator.external.ShipmentsApi;
import com.xe.fdx.aggregator.external.TrackApi;
import com.xe.fdx.aggregator.model.Aggregation;
import com.xe.fdx.aggregator.model.TrackStatus;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationService {

  public final ShipmentsApi shipmentsApi;
  public final TrackApi trackApi;

  public final PricingApi pricingApi;

  @Value("${tracking.api.timeout}")
  private int trackingApiTimout;
  @Value("${shipment.api.timeout}")
  private int shippingApiTimout;
  @Value("${pricing.api.timeout}")
  private int pricingApiTimout;

  public Mono<Aggregation> getAggregation(List<String> shipmentsOrderNumbers,
      List<String> trackOrderNumbers, List<String> pricingCountryCodes) {
    return Mono.zip(getShipmentsFlux(shipmentsOrderNumbers), getTrackStatusFlux(trackOrderNumbers),
            getPricingFlux(pricingCountryCodes))
        .map(
            t3 -> new Aggregation(t3.getT1().stream()
                .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)),
                t3.getT2().stream()
                    .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)),
                t3.getT3().stream().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2))));
  }

  private Mono<List<Tuple2<String, List<String>>>> getShipmentsFlux(
      List<String> shipmentsOrderNumbers) {
    return Flux.fromIterable(shipmentsOrderNumbers).flatMap(this::getShipment).collectList();
  }

  private Mono<Tuple2<String, List<String>>> getShipment(String shipmentOrderNumber) {
    return shipmentsApi.getShipments(shipmentOrderNumber)
        .timeout(Duration.ofSeconds(shippingApiTimout))
        .doOnError(
            e -> log.info("Failed to get shipment status for {}. Error was: {}",
                shipmentOrderNumber,
                e.getMessage()))
        .onErrorResume(e -> Mono.empty())
        .map(l -> Tuples.of(shipmentOrderNumber, l));
  }

  private Mono<List<Tuple2<String, TrackStatus>>> getTrackStatusFlux(
      List<String> trackOrderNumbers) {
    return Flux.fromIterable(trackOrderNumbers).flatMap(this::getTrackingStatus).collectList();
  }

  private Mono<Tuple2<String, TrackStatus>> getTrackingStatus(String trackOrderNumber) {
    return trackApi.getTrackingStatus(trackOrderNumber)
        .timeout(Duration.ofSeconds(trackingApiTimout))
        .doOnError(
            e -> log.info("Failed to get tracking status for {}. Error was: {}", trackOrderNumber,
                e.getMessage()))
        .onErrorResume(e -> Mono.empty())
        .map(l -> Tuples.of(trackOrderNumber, l));
  }

  private Mono<List<Tuple2<String, BigDecimal>>> getPricingFlux(
      List<String> countryCodes) {
    return Flux.fromIterable(countryCodes).flatMap(this::getPricing).collectList();
  }

  private Mono<Tuple2<String, BigDecimal>> getPricing(String country) {
    return pricingApi.getPricing(country).timeout(Duration.ofSeconds(pricingApiTimout)).doOnError(
            e -> log.info("Failed to get pricing for {}. Error was: {}", country, e.getMessage()))
        .onErrorResume(e -> Mono.empty()).map(l -> Tuples.of(country, l));
  }
}
