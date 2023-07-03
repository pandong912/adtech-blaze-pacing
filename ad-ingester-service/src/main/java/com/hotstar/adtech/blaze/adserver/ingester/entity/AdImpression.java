package com.hotstar.adtech.blaze.adserver.ingester.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdImpression {
  private String creativeId;
  private Long impression;
}
