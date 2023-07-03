package com.hotstar.adtech.blaze.exchanger.api.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompareResponseDetail {
  String adSetId;
  Long beacon;
  Long pulse;
  Double percentage;
}
