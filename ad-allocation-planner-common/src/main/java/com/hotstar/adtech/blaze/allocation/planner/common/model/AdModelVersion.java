package com.hotstar.adtech.blaze.allocation.planner.common.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Builder
@Getter
@EqualsAndHashCode(of = {"adModelMd5", "metadataMd5", "liveMatchMd5"})
public class AdModelVersion {
  public static final AdModelVersion EMPTY = AdModelVersion.builder()
    .version(-1L)
    .id(-1L)
    .metadataMd5(StringUtils.EMPTY)
    .adModelMd5(StringUtils.EMPTY)
    .liveMatchMd5(StringUtils.EMPTY)
    .path(StringUtils.EMPTY)
    .build();
  private final Long version;
  private final String path;
  private final Long id;
  private final String adModelMd5;
  private final String metadataMd5;
  private final String liveMatchMd5;

}
