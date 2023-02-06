package com.xe.fdx.aggregator.business;

import static com.xe.fdx.aggregator.model.TrackStatus.COLLECTING;
import static com.xe.fdx.aggregator.model.TrackStatus.IN_TRANSIT;
import static org.mockito.Mockito.when;

import com.xe.fdx.aggregator.external.PricingApi;
import com.xe.fdx.aggregator.external.ShipmentsApi;
import com.xe.fdx.aggregator.external.TrackApi;
import com.xe.fdx.aggregator.model.Aggregation;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import javax.naming.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AggregatorServiceTest {

  @InjectMocks
  private AggregationService aggregationService;

  @Mock
  private PricingApi pricingApi;
  @Mock
  private ShipmentsApi shipmentsApi;
  @Mock
  private TrackApi trackApi;

  @Test
  void getAggregate() {
    when(shipmentsApi.getShipments("000000001")).thenReturn(Mono.just(List.of("BOX", "PALLET")));
    when(shipmentsApi.getShipments("000000002")).thenReturn(Mono.just(List.of("ENVELOPE")));
    when(trackApi.getTrackingStatus("000000003")).thenReturn(Mono.just(COLLECTING));
    when(trackApi.getTrackingStatus("000000004")).thenReturn(Mono.just(IN_TRANSIT));
    when(pricingApi.getPricing("NL")).thenReturn(Mono.just(BigDecimal.valueOf(12.24)));
    when(pricingApi.getPricing("DE")).thenReturn(Mono.just(BigDecimal.valueOf(2.24677)));

    Aggregation expected = new Aggregation(
        Map.of("000000001", List.of("BOX", "PALLET"), "000000002", List.of("ENVELOPE")),
        Map.of("000000003", COLLECTING, "000000004", IN_TRANSIT),
        Map.of("NL", BigDecimal.valueOf(12.24), "DE", BigDecimal.valueOf(2.24677)));

    Mono<Aggregation> aggregation = aggregationService.getAggregation(
        List.of("000000001", "000000002"),
        List.of("000000003", "000000004"),
        List.of("NL", "DE"));

    StepVerifier.create(aggregation).expectNext(expected).expectComplete().verify();
  }

  @Test
  void getAggregateWhenGetShipmentsFails() {
    when(shipmentsApi.getShipments("000000001")).thenReturn(Mono.just(List.of("BOX", "PALLET")));
    when(shipmentsApi.getShipments("000000002")).thenReturn(
        Mono.error(new ServiceUnavailableException()));
    when(trackApi.getTrackingStatus("000000003")).thenReturn(Mono.just(COLLECTING));
    when(pricingApi.getPricing("NL")).thenReturn(Mono.just(BigDecimal.valueOf(12.24)));

    Aggregation expected = new Aggregation(
        Map.of("000000001", List.of("BOX", "PALLET")),
        Map.of("000000003", COLLECTING),
        Map.of("NL", BigDecimal.valueOf(12.24)));

    Mono<Aggregation> aggregation = aggregationService.getAggregation(
        List.of("000000001", "000000002"),
        List.of("000000003"),
        List.of("NL"));

    StepVerifier.create(aggregation).expectNext(expected).expectComplete().verify();
  }

  @Test
  void getAggregateWhenGetTrackingFails() {
    when(shipmentsApi.getShipments("000000001")).thenReturn(Mono.just(List.of("BOX", "PALLET")));
    when(trackApi.getTrackingStatus("000000003")).thenReturn(Mono.just(COLLECTING));
    when(trackApi.getTrackingStatus("000000004")).thenReturn(
        Mono.error(new ServiceUnavailableException()));
    when(pricingApi.getPricing("NL")).thenReturn(Mono.just(BigDecimal.valueOf(12.24)));

    Aggregation expected = new Aggregation(
        Map.of("000000001", List.of("BOX", "PALLET")),
        Map.of("000000003", COLLECTING),
        Map.of("NL", BigDecimal.valueOf(12.24)));

    Mono<Aggregation> aggregation = aggregationService.getAggregation(
        List.of("000000001"),
        List.of("000000003", "000000004"),
        List.of("NL"));

    StepVerifier.create(aggregation).expectNext(expected).expectComplete().verify();
  }

  @Test
  void getAggregateWhenGetPricingFails() {
    when(shipmentsApi.getShipments("000000001")).thenReturn(Mono.just(List.of("BOX", "PALLET")));
    when(trackApi.getTrackingStatus("000000003")).thenReturn(Mono.just(COLLECTING));
    when(pricingApi.getPricing("NL")).thenReturn(Mono.just(BigDecimal.valueOf(12.24)));
    when(pricingApi.getPricing("DE")).thenReturn(
        Mono.error(new ServiceUnavailableException()));

    Aggregation expected = new Aggregation(
        Map.of("000000001", List.of("BOX", "PALLET")),
        Map.of("000000003", COLLECTING),
        Map.of("NL", BigDecimal.valueOf(12.24)));

    Mono<Aggregation> aggregation = aggregationService.getAggregation(
        List.of("000000001"),
        List.of("000000003"),
        List.of("NL", "DE"));

    StepVerifier.create(aggregation).expectNext(expected).expectComplete().verify();
  }

  @Test
  void getAggregateWhenGetShipmentsTimesOut() {
    when(shipmentsApi.getShipments("000000001")).thenReturn(Mono.just(List.of("BOX", "PALLET")));
    when(shipmentsApi.getShipments("000000002")).thenReturn(
        Mono.error(new TimeoutException("API call exceeded the timeout")));
    when(trackApi.getTrackingStatus("000000003")).thenReturn(Mono.just(COLLECTING));
    when(pricingApi.getPricing("NL")).thenReturn(Mono.just(BigDecimal.valueOf(12.24)));

    Aggregation expected = new Aggregation(
        Map.of("000000001", List.of("BOX", "PALLET")),
        Map.of("000000003", COLLECTING),
        Map.of("NL", BigDecimal.valueOf(12.24)));

    Mono<Aggregation> aggregation = aggregationService.getAggregation(
        List.of("000000001", "000000002"),
        List.of("000000003"),
        List.of("NL"));

    StepVerifier.create(aggregation).expectNext(expected).expectComplete().verify();
  }

  @Test
  void getAggregateWhenGetTrackingTimesOut() {
    when(shipmentsApi.getShipments("000000001")).thenReturn(Mono.just(List.of("BOX", "PALLET")));
    when(trackApi.getTrackingStatus("000000003")).thenReturn(Mono.just(COLLECTING));
    when(trackApi.getTrackingStatus("000000004")).thenReturn(
        Mono.error(new TimeoutException("API call exceeded the timeout")));
    when(pricingApi.getPricing("NL")).thenReturn(Mono.just(BigDecimal.valueOf(12.24)));

    Aggregation expected = new Aggregation(
        Map.of("000000001", List.of("BOX", "PALLET")),
        Map.of("000000003", COLLECTING),
        Map.of("NL", BigDecimal.valueOf(12.24)));

    Mono<Aggregation> aggregation = aggregationService.getAggregation(
        List.of("000000001"),
        List.of("000000003", "000000004"),
        List.of("NL"));

    StepVerifier.create(aggregation).expectNext(expected).expectComplete().verify();
  }

  @Test
  void getAggregateWhenGetPricingTimesOut() {
    when(shipmentsApi.getShipments("000000001")).thenReturn(Mono.just(List.of("BOX", "PALLET")));
    when(trackApi.getTrackingStatus("000000003")).thenReturn(Mono.just(COLLECTING));
    when(pricingApi.getPricing("NL")).thenReturn(Mono.just(BigDecimal.valueOf(12.24)));
    when(pricingApi.getPricing("DE")).thenReturn(
        Mono.error(new TimeoutException("API call exceeded the timeout")));

    Aggregation expected = new Aggregation(
        Map.of("000000001", List.of("BOX", "PALLET")),
        Map.of("000000003", COLLECTING),
        Map.of("NL", BigDecimal.valueOf(12.24)));

    Mono<Aggregation> aggregation = aggregationService.getAggregation(
        List.of("000000001"),
        List.of("000000003"),
        List.of("NL", "DE"));

    StepVerifier.create(aggregation).expectNext(expected).expectComplete().verify();
  }
}
