package com.hotstar.adtech.blaze.ingester.controller;

import com.hotstar.adtech.blaze.ingester.entity.Ad;
import com.hotstar.adtech.blaze.ingester.entity.AdModelVersion;
import com.hotstar.adtech.blaze.ingester.entity.Match;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdModelResponse {
  private List<Match> matches;
  private Map<Long, Map<String, String>> streamMappingConverterGroup;
  private Map<String, String> globalStreamMappingConverter;
  private Map<String, Ad> adMap;
  private AdModelVersion adModelVersion;
}
