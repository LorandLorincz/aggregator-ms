package com.xe.fdx.aggregator.business;

import com.xe.fdx.aggregator.external.ShipmentsApi;
import com.xe.fdx.aggregator.model.Aggregation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
public class AggregationService {

  public final ShipmentsApi shipmentsApi;

  public Mono<Aggregation> getAggregation(List<String> shipmentsOrderNumbers,
      List<String> trackOrderNumbers, List<String> pricingCountryCodes) {
    return getShipmentsList(shipmentsOrderNumbers).map(
        s -> new Aggregation(s.stream()
            .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)),
            Collections.emptyMap(),
            Collections.emptyMap()));
  }

  private Mono<List<Tuple2<String, List<String>>>> getShipmentsList(
      List<String> shipmentsOrderNumbers) {
    return Flux.fromIterable(shipmentsOrderNumbers)
        .flatMap(o -> shipmentsApi.getShipments(o).map(l -> Tuples.of(o, l)))
        .collectList();
  }
}
