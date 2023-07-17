package com.hotstar.adtech.blaze.exchanger.api.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ContentStreamConcurrencyResponse {
  String playoutId;
  Long concurrencyValue;

  public String getKey() {
    return playoutId;
  }
}
