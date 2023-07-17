package com.hotstar.adtech.blaze.ingester.entity;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ConcurrencyGroup {
  private Long tsBucket;
  private Map<String, Long> concurrencyValues;
}
