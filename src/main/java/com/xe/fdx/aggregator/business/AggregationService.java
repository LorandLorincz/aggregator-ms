package com.xe.fdx.aggregator.business;

import com.xe.fdx.aggregator.external.PricingApi;
import com.xe.fdx.aggregator.external.ShipmentsApi;
import com.xe.fdx.aggregator.external.TrackApi;
import com.xe.fdx.aggregator.model.Aggregation;
import com.xe.fdx.aggregator.model.TrackStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    return Flux.fromIterable(shipmentsOrderNumbers)
        .flatMap(o -> shipmentsApi.getShipments(o).doOnError(
                e -> log.info("Failed to get shipment status for {}. Error was: {}", o, e.getMessage()))
            .onErrorResume(e -> Mono.empty())
            .map(l -> Tuples.of(o, l)))
        .collectList();
  }

  private Mono<List<Tuple2<String, TrackStatus>>> getTrackStatusFlux(
      List<String> trackOrderNumbers) {
    return Flux.fromIterable(trackOrderNumbers)
        .flatMap(o -> trackApi.getTrackingStatus(o).doOnError(
                e -> log.info("Failed to get tracking status for {}. Error was: {}", o, e.getMessage()))
            .onErrorResume(e -> Mono.empty())
            .map(l -> Tuples.of(o, l)))
        .collectList();
  }

  private Mono<List<Tuple2<String, BigDecimal>>> getPricingFlux(
      List<String> countryCodes) {
    return Flux.fromIterable(countryCodes)
        .flatMap(c -> pricingApi.getPricing(c).doOnError(
                e -> log.info("Failed to get pricing for {}. Error was: {}", c, e.getMessage()))
            .onErrorResume(e -> Mono.empty())
            .onErrorResume(e -> Mono.empty()).map(l -> Tuples.of(c, l)))
        .collectList();
  }
}
