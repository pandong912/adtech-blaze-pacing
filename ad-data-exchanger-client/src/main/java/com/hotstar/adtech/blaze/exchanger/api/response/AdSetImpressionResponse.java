package com.hotstar.adtech.blaze.exchanger.api.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AdSetImpressionResponse {
  Long adSetId;
  Long impression;
}
