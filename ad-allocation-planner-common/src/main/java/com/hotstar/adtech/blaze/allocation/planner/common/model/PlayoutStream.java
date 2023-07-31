package com.hotstar.adtech.blaze.allocation.planner.common.model;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
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
  Language language;
  List<Platform> platforms;
  String key;
  List<Integer> platformIds;
  String playoutId;
  StreamType streamType;

  @Builder
  public PlayoutStream(String playoutId, StreamType streamType, Tenant tenant, Language language,
                       List<Platform> platforms) {
    this.playoutId = playoutId;
    this.streamType = streamType;
    this.tenant = tenant;
    this.language = language;
    this.platforms = platforms;
    this.key = generateKey();
    this.platformIds = platforms.stream().map(Platform::getId).collect(Collectors.toList());
  }

  private String generateKey() {
    return (tenant == null ? "" : tenant.getName()) + SPLITTER + language.getName() + SPLITTER
      + getPlatformString();
  }

  private String getPlatformString() {
    return platforms.stream().map(Platform::getName).collect(Collectors.joining("+"));
  }

}
