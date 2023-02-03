package com.xe.fdx.aggregator.business;

import com.xe.fdx.aggregator.model.Aggregation;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AggregationService {

  public Mono<Aggregation> getAggregation(List<String> shipmentsOrderNumbers,
      List<String> trackOrderNumbers, List<String> pricingCountryCodes) {
    return Mono.just(
        new Aggregation(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()));
  }
}
