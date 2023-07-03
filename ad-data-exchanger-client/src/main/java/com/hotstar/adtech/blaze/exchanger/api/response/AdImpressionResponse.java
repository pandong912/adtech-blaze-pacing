package com.hotstar.adtech.blaze.exchanger.api.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdImpressionResponse {

  private String creativeId;
  private Long impression;
}
