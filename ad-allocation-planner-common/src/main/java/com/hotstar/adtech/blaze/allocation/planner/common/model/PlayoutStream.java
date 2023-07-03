package com.hotstar.adtech.blaze.allocation.planner.common.model;

import com.hotstar.adtech.blaze.admodel.common.entity.LanguageEntity;
import com.hotstar.adtech.blaze.admodel.common.entity.PlatformEntity;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;

@Value
public class PlayoutStream {

  private static final String SPLITTER = "-";
  private static final String PLATFORM_SPLITTER = "+";
  Tenant tenant;
  LanguageEntity language;
  List<PlatformEntity> platforms;
  String key;
  List<Integer> platformIds;

  @Builder
  public PlayoutStream(Tenant tenant, LanguageEntity language, List<PlatformEntity> platforms) {
    this.tenant = tenant;
    this.language = language;
    this.platforms = platforms;
    this.key = generateKey();
    this.platformIds = platforms.stream().map(PlatformEntity::getId).collect(Collectors.toList());
  }

  private String generateKey() {
    return (tenant == null ? "" : tenant.getName()) + SPLITTER + language.getName() + SPLITTER
      + getPlatformString();
  }

  private String getPlatformString() {
    return platforms.stream().map(PlatformEntity::getName).collect(Collectors.joining("+"));
  }
}
