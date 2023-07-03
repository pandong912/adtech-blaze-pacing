package com.hotstar.adtech.blaze.exchanger.api.response;

import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDetail;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ContentCohortConcurrencyResponse {
  String ssaiTag;
  StreamDetail streamDetail;
  Long concurrencyValue;

  public String getKey() {
    return streamDetail.getKey() + "|" + ssaiTag;
  }
}
