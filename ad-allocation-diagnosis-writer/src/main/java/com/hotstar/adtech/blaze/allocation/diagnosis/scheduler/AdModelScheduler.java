package com.hotstar.adtech.blaze.allocation.diagnosis.scheduler;

import com.hotstar.adtech.blaze.admodel.client.AdModelClient;
import com.hotstar.adtech.blaze.admodel.client.AdModelUri;
import com.hotstar.adtech.blaze.admodel.client.entity.LiveEntities;
import com.hotstar.adtech.blaze.admodel.client.entity.MatchEntities;
import com.hotstar.adtech.blaze.admodel.repository.AdModelResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.AdModelSyncPointRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AdModelResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AdModelSyncPoint;
import com.hotstar.adtech.blaze.allocation.diagnosis.entity.AdModelData;
import com.hotstar.adtech.blaze.allocation.diagnosis.service.AdModelAdService;
import com.hotstar.adtech.blaze.allocation.diagnosis.service.AdModelAdSetMatchService;
import com.hotstar.adtech.blaze.allocation.diagnosis.service.AdModelMatchService;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdModelScheduler {
  private final AdModelClient adModelClient;
  private final DataExchangerClient dataExchangerClient;
  private final AdModelAdSetMatchService adModelAdSetMatchService;
  private final AdModelAdService adModelAdService;
  private final AdModelMatchService adModelMatchService;
  private final AdModelResultRepository adModelResultRepository;
  private final AdModelSyncPointRepository adModelSyncPointRepository;

  @Scheduled(fixedDelayString = "30000")
  public void update() {

    AdModelSyncPoint syncPoint = adModelSyncPointRepository
      .findFirstByOrderByIdDesc()
      .orElseGet(this::buildDefaultSyncPoint);

    List<AdModelResult> adModelResults =
      adModelResultRepository.findFirst20ByIdGreaterThanOrderById(syncPoint.getAdModelResultId());

    Long maxId = adModelResults.stream()
      .mapToLong(AdModelResult::getId)
      .max()
      .orElse(syncPoint.getAdModelResultId());
    if (maxId <= syncPoint.getAdModelResultId()) {
      return;
    }

    List<AdModelUri> adModelUris = adModelResults.stream()
      .map(this::buildAdModelUri)
      .collect(Collectors.toList());


    adModelUris.stream()
      .map(this::loadData)
      .forEach(this::writeToClickHouse);
    AdModelSyncPoint newSyncPoint = AdModelSyncPoint.builder()
      .adModelResultId(maxId)
      .build();
    adModelSyncPointRepository.save(newSyncPoint);
    log.info("update ad model sync point to {}", maxId);
  }

  private void writeToClickHouse(AdModelData adModelData) {
    adModelAdSetMatchService.writeMatchAdSet(adModelData.getLiveEntities().getGoalMatches(), adModelData.getVersion());
    adModelAdService.writeAd(adModelData.getLiveEntities().getAds(), adModelData.getVersion());
    adModelMatchService.writeMatch(adModelData.getMatchEntities().getMatches(), adModelData.getVersion());
  }


  private AdModelUri buildAdModelUri(AdModelResult adModelResult) {
    return AdModelUri.builder()
      .version(adModelResult.getVersion().toEpochMilli())
      .id(adModelResult.getId())
      .path(adModelResult.getPath())
      .build();
  }

  private AdModelData loadData(AdModelUri adModelUri) {
    LiveEntities liveEntities = adModelClient.loadLiveAdModel(adModelUri);
    MatchEntities matchEntities = adModelClient.loadMatch(adModelUri);
    return AdModelData.builder()
      .version(Instant.ofEpochMilli(adModelUri.getVersion()))
      .liveEntities(liveEntities)
      .matchEntities(matchEntities)
      .build();
  }

  private AdModelSyncPoint buildDefaultSyncPoint() {
    return AdModelSyncPoint.builder()
      .adModelResultId(-1L)
      .build();
  }

}
