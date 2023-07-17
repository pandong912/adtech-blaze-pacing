package com.hotstar.adtech.blaze.exchanger.api.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MatchProgressModelResponse {
  List<Double> deliveryProgresses;
}
