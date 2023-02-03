package com.xe.fdx.aggregator.api;

import com.xe.fdx.aggregator.business.AggregationService;
import com.xe.fdx.aggregator.model.Aggregation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "aggregation", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AggregatorController {

    public final AggregationService aggregationService;

    @GetMapping
    public Mono<Aggregation> getAggregation(
        @RequestParam(required = false) List<String> shipmentsOrderNumbers,
        @RequestParam(required = false) List<String> trackOrderNumbers,
        @RequestParam(required = false) List<String> pricingCountryCodes) {
        return aggregationService.getAggregation(shipmentsOrderNumbers, trackOrderNumbers,
            pricingCountryCodes);
    }
}
