package com.hotstar.adtech.blaze.allocation.diagnosis.service;

import static com.hotstar.adtech.blaze.allocation.diagnosis.metric.MetricNames.AD_MODEL_MATCH;

import com.hotstar.adtech.blaze.admodel.client.model.MatchInfo;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AdModelMatch;
import com.hotstar.adtech.blaze.allocation.diagnosis.sink.AdModelMatchSink;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdModelMatchService {
  private final AdModelMatchSink adModelMatchSink;

  @Timed(AD_MODEL_MATCH)
  public void writeMatch(List<MatchInfo> matchInfos, Instant version) {
    List<AdModelMatch> collect = matchInfos.stream()
      .map(match -> AdModelMatch.builder()
        .siMatchId(match.getSiMatchId())
        .version(version)
        .id(match.getId())
        .matchVersion(match.getVersion())
        .name(match.getName())
        .contentId(match.getContentId())
        .status(match.getStatus().toString())
        .languageId(match.getLanguageId())
        .seasonId(match.getSeasonId())
        .startTime(match.getStartTime())
        .tournamentId(match.getTournamentId())
        .seasonId(match.getSeasonId())
        .build()
      ).collect(Collectors.toList());
    adModelMatchSink.write(collect);
  }
}
