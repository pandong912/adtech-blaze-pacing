package com.hotstar.adtech.blaze.ingester.task;

import com.hotstar.adtech.blaze.ingester.entity.Match;
import com.hotstar.adtech.blaze.ingester.service.AdModelLoader;
import com.hotstar.adtech.blaze.ingester.service.ConcurrencyService;
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
    for (Match match : adModelLoader.get().getMatches()) {
      concurrencyService.updateMatchConcurrency(match);
    }
  }

}
