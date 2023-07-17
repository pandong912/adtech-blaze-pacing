package com.hotstar.adtech.blaze.exchanger.api.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdImpressionResponse {

  String creativeId;
  Long impression;
}
