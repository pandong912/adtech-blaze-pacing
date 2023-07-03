package com.hotstar.adtech.blaze.exchanger.api.entity;

import com.hotstar.adtech.blaze.admodel.common.entity.LanguageEntity;
import com.hotstar.adtech.blaze.admodel.common.entity.PlatformEntity;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StreamDetail {

  static final String SPLITTER = "-";
  static final String PLATFORM_SPLITTER = "\\+";
  Tenant tenant;
  LanguageEntity language;
  // should always be sorted by id
  List<PlatformEntity> platforms;

  public String getKey() {
    return (tenant == null ? "" : tenant.getName()) + SPLITTER + language.getName() + SPLITTER
      + platforms.stream().map(PlatformEntity::getName).collect(Collectors.joining("+"));
  }

  public static StreamDetail fromString(String streamKey, PlatformMapping platformMapping,
                                        LanguageMapping languageMapping) {
    String[] streamTags = streamKey.split(SPLITTER, -1);
    String tenant = streamTags.length > 0 ? streamTags[0] : "";
    String language = streamTags.length > 1 ? streamTags[1] : "";
    String platforms = streamTags.length > 2 ? streamTags[2] : "";
    return StreamDetail.builder()
      .tenant(Tenant.fromName(tenant))
      .language(languageMapping.getByName(language))
      .platforms(getPlatformList(platforms, platformMapping))
      .build();
  }

  private static List<PlatformEntity> getPlatformList(String platforms, PlatformMapping platformMapping) {
    return Arrays.stream(platforms.split(PLATFORM_SPLITTER))
      .map(platformMapping::getByName)
      .sorted(Comparator.comparingInt(PlatformEntity::getId))
      .collect(Collectors.toList());
  }


  public List<String> toSinglePlatformKey() {
    return platforms.stream()
      .map(platform -> (tenant == null ? "" : tenant.getName()) + SPLITTER + language.getName() + SPLITTER
        + platform.getName())
      .collect(Collectors.toList());
  }
}
