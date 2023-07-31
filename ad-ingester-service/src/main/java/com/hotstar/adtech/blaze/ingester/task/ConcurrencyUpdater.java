package com.hotstar.adtech.blaze.ingester.task;

import com.hotstar.adtech.blaze.ingester.entity.AdModel;
import com.hotstar.adtech.blaze.ingester.entity.Match;
import com.hotstar.adtech.blaze.ingester.service.AdModelLoader;
import com.hotstar.adtech.blaze.ingester.service.ConcurrencyService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConcurrencyUpdater {

  private final AdModelLoader adModelLoader;
  private final ConcurrencyService concurrencyService;

  @Scheduled(fixedDelayString = "${blaze.ad-ingester-service.schedule.concurrency-sync-delay:20000}")
  public void update() {
    AdModel adModel = adModelLoader.get();
    for (Match match : adModel.getMatches()) {
      Map<String, String> streamMappingConverter = adModel.getStreamMappingConverter(match.getSeasonId());
      concurrencyService.updateMatchConcurrency(match, streamMappingConverter);
    }
  }

}
