package com.hotstar.adtech.blaze.ingester.entity;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AdModelVersion {
  private final Long version;
  private final String adEntityMd5;
  private final String liveMatchMd5;
}
