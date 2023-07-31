package com.hotstar.adtech.blaze.ingester.entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Builder
@Getter
@Slf4j
public class AdModel {
  private final List<Match> matches;
  private final Map<Long, Map<String, String>> streamMappingConverterGroup;
  private final Map<String, String> globalStreamMappingConverter;
  private final Map<String, Ad> adMap;
  private final AdModelVersion adModelVersion;

  public Map<String, String> getStreamMappingConverter(Long seasonId) {
    Map<String, String> streamMappingConverter = streamMappingConverterGroup.get(seasonId);
    if (Objects.isNull(streamMappingConverter)) {
      log.warn("season stream mapping converter is not existed, seasonId: {}", seasonId);
      return globalStreamMappingConverter;
    }
    return streamMappingConverter;
  }
}
