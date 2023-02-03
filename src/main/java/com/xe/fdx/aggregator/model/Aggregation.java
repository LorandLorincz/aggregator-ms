package com.xe.fdx.aggregator.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record Aggregation(Map<String, List<String>> shipments, Map<String, String> track,
                          Map<String, BigDecimal> pricing) {

}
