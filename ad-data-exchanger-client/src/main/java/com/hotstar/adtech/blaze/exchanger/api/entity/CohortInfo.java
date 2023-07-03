package com.hotstar.adtech.blaze.exchanger.api.entity;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CohortInfo {
  StreamDetail streamDetail;
  String ssaiTag;

  public String getRedisKey() {
    return streamDetail.getKey() + "|" + ssaiTag;
  }
}
