package com.hotstar.adtech.blaze.exchanger.api.response;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompareResponse {
  List<CompareResponseDetail> compareResponseDetailList;
  Map<String, Long> beacon;
  Map<String, Long> pulse;
}
