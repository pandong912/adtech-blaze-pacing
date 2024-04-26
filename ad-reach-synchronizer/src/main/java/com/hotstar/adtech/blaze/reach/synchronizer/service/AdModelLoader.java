package com.hotstar.adtech.blaze.reach.synchronizer.service;

import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import com.hotstar.adtech.blaze.exchanger.api.response.admodel.AdModelForSynchronizerResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.admodel.AdSetResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.admodel.MatchResponse;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.AdModel;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.AdSet;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.Match;
import com.hotstar.adtech.blaze.reach.synchronizer.metric.MetricNames;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdModelLoader {
  private final DataExchangerClient dataExchangerClient;
  private final AtomicReference<AdModel> adModelAtomicReference = new AtomicReference<>();

  @PostConstruct
  public void init() {
    loadAdModel();
  }

  @Scheduled(fixedDelayString = "${blaze.ad-reach-synchronizer.load.ad-model:10000}")
  @Timed(MetricNames.LOAD_AD_MODEL)
  public void loadAdModel() {
    try {
      AdModelForSynchronizerResponse adModelForSyncResponse = dataExchangerClient.getLatestAdModelForSynchronizer();

      AdModel adModel = AdModel.builder()
        .matches(buildMatches(adModelForSyncResponse))
        .contentIdToAdSets(adModelForSyncResponse
          .getContentIdToAdSets()
          .entrySet()
          .stream()
          .collect(Collectors.toMap(Map.Entry::getKey, AdModelLoader::buildAdSets)))
        .versionTimestamp(adModelForSyncResponse.getAdModelVersion().getVersionTimestamp())
        .build();

      adModelAtomicReference.set(adModel);
      LoadStatus.SUCCESS.counter().increment();
    } catch (Exception ex) {
      LoadStatus.FAILED.counter().increment();
      log.error("Load Live Match Model failed", ex);
    }
  }

  private static List<AdSet> buildAdSets(Map.Entry<String, List<AdSetResponse>> entry) {
    return entry
      .getValue()
      .stream()
      .map(AdModelLoader::buildAdSet)
      .collect(Collectors.toList());
  }

  private static AdSet buildAdSet(AdSetResponse adSetResponse) {
    return AdSet.builder()
      .contentId(adSetResponse.getContentId())
      .maximiseReach(adSetResponse.getMaximiseReach())
      .id(adSetResponse.getAdSetId())
      .campaignId(adSetResponse.getCampaignId())
      .build();
  }

  private static List<Match> buildMatches(AdModelForSynchronizerResponse adModelForSyncResponse) {
    return adModelForSyncResponse
      .getMatches()
      .stream()
      .map(AdModelLoader::buildMatch)
      .collect(Collectors.toList());
  }

  private static Match buildMatch(MatchResponse matchResponse) {
    return Match.builder()
      .siMatchId(matchResponse.getSiMatchId())
      .contentId(matchResponse.getContentId())
      .build();
  }

  public AdModel get() {
    return adModelAtomicReference.get();
  }


  private enum LoadStatus {
    IGNORE, URL_NULL, FAILED, SUCCESS;
    private final Counter counter;

    LoadStatus() {
      this.counter = Metrics.counter(MetricNames.LOAD_AD_MODEL_STATUS, "status", name().toLowerCase(
        Locale.ROOT));
    }

    private Counter counter() {
      return counter;
    }
  }

}
