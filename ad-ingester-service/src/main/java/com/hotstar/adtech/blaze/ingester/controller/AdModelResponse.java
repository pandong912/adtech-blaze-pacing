package com.hotstar.adtech.blaze.ingester.controller;

import com.hotstar.adtech.blaze.ingester.entity.Ad;
import com.hotstar.adtech.blaze.ingester.entity.AdModelVersion;
import com.hotstar.adtech.blaze.ingester.entity.Match;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdModelResponse {
  List<Match> matches;
  Map<Long, Map<String, String>> streamMappingConverterGroup;
  Map<String, String> globalStreamMappingConverter;
  Map<String, Ad> adMap;
  AdModelVersion adModelVersion;
}
