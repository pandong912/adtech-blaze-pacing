package com.hotstar.adtech.blaze.exchanger.api.response;

import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDetail;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SampleConcurrencyResponse {
  String ssaiTag;
  StreamDetail streamDetail;
  long sampleConcurrency;
}
