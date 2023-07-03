package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.admodel.common.exception.ResourceNotFoundException;
import com.hotstar.adtech.blaze.admodel.repository.BreakTypeRepository;
import com.hotstar.adtech.blaze.admodel.repository.MatchBreakRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.BreakType;
import com.hotstar.adtech.blaze.adserver.data.redis.service.RuntimeMatchBreakRepository;
import com.hotstar.adtech.blaze.exchanger.api.entity.BreakId;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakListResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakTypeResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BreakService {
  private final MatchBreakRepository matchBreakRepository;
  private final BreakTypeRepository breakTypeRepository;

  private final RuntimeMatchBreakRepository runtimeMatchBreakRepository;

  public List<BreakListResponse> getBreakList(String contentId) {
    Map<String, Map<String, Long>> streamBreaks =
      runtimeMatchBreakRepository.getMatchStreamBreaks(contentId);
    return streamBreaks.entrySet().stream()
      .map(entry -> fromStreamBreaks(entry.getKey(), entry.getValue()))
      .collect(Collectors.toList());
  }

  public BreakListResponse getBreakListByStream(String contentId, String playoutId) {
    Map<String, Map<String, Long>> streamBreakList =
      runtimeMatchBreakRepository.getMatchStreamBreaks(contentId);
    return fromStreamBreaks(playoutId, streamBreakList.getOrDefault(playoutId, Collections.emptyMap()));
  }

  public Integer getTotalBreakNumber(String contentId) {
    return matchBreakRepository.findByContentId(contentId)
      .map(matchBreak -> matchBreak.getBreaksLeft() + matchBreak.getBreaksConsumed())
      .orElseThrow(() -> new ResourceNotFoundException("Match Break not found for contentId: " + contentId));
  }

  public List<BreakTypeResponse> getAllBreakType() {
    List<BreakType> breakTypes = breakTypeRepository.findAll();
    return breakTypes.stream()
      .map(breakType ->
        BreakTypeResponse.builder()
          .id(breakType.getId())
          .type(breakType.getType())
          .name(breakType.getName())
          .durationLowerBound(breakType.getDurationLowerBound())
          .durationUpperBound(breakType.getDurationUpperBound())
          .step(breakType.getStep())
          .duration(breakType.getDuration()).build())
      .collect(Collectors.toList());
  }

  private BreakListResponse fromStreamBreaks(String playoutId, Map<String, Long> breakMap) {
    return BreakListResponse.builder()
      .playoutId(playoutId)
      .breakIds(breakMap.entrySet().stream()
        .map(breakEntry -> BreakId.builder()
          .breadId(breakEntry.getKey())
          .timestamp(breakEntry.getValue())
          .build())
        .collect(Collectors.toList()))
      .build();
  }
}
