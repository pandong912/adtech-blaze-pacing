package com.hotstar.adtech.blaze.reach.synchronizer.task;

import com.hotstar.adtech.blaze.reach.synchronizer.config.launchdarkly.BlazeDynamicConfig;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.AdModel;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.AdSet;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.Match;
import com.hotstar.adtech.blaze.reach.synchronizer.service.AdModelLoader;
import com.hotstar.adtech.blaze.reach.synchronizer.service.ReachService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReachDataUpdater {

  private final AdModelLoader adModelLoader;
  private final ReachService reachService;
  private final BlazeDynamicConfig blazeDynamicConfig;

  @Scheduled(fixedDelayString = "${blaze.ad-reach-synchronizer.schedule.reach-sync-delay:60000}")
  public void update() {
    if (!blazeDynamicConfig.getEnableMaximiseReach()) {
      return;
    }
    AdModel adModel = adModelLoader.get();
    for (Match match : adModel.getMatches()) {
      List<AdSet> adSets = adModel.getContentIdToAdSets().get(match.getContentId());
      if (adSets == null || adSets.isEmpty()) {
        log.info("no adSet found for match: {}", match);
        continue;
      }
      Map<Long, Boolean> adSetMaximiseReach =
        adSets.stream().collect(Collectors.toMap(AdSet::getId, this::getMaximiseReach));
      reachService.updateMatchReachMatch(match, adSetMaximiseReach);
    }
  }

  private Boolean getMaximiseReach(AdSet adSet) {
    return adSet.getMaximiseReach() != null && adSet.getMaximiseReach();
  }
}
