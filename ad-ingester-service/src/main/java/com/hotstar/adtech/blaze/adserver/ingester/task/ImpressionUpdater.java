package com.hotstar.adtech.blaze.adserver.ingester.task;

import com.hotstar.adtech.blaze.adserver.ingester.entity.Ad;
import com.hotstar.adtech.blaze.adserver.ingester.entity.AdModel;
import com.hotstar.adtech.blaze.adserver.ingester.entity.Match;
import com.hotstar.adtech.blaze.adserver.ingester.service.AdModelLoader;
import com.hotstar.adtech.blaze.adserver.ingester.service.ImpressionService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Deprecated
public class ImpressionUpdater {

  private final AdModelLoader adModelLoader;
  private final ImpressionService impressionService;

  @Scheduled(fixedDelayString = "${blaze.ad-ingester-service.schedule.impression-sync-delay:10000}")
  public void update() {
    AdModel adModel = adModelLoader.get();
    Map<String, Ad> adMap = adModel.getAdMap();
    for (Match match : adModel.getMatches()) {
      impressionService.updateImpression(match, adMap);
    }
  }

}
