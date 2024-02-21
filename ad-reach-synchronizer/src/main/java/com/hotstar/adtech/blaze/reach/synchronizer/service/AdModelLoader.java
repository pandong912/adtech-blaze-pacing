package com.hotstar.adtech.blaze.reach.synchronizer.service;

import com.hotstar.adtech.blaze.admodel.client.AdModelClient;
import com.hotstar.adtech.blaze.admodel.client.AdModelUri;
import com.hotstar.adtech.blaze.admodel.client.common.Names;
import com.hotstar.adtech.blaze.admodel.client.entity.LiveEntities;
import com.hotstar.adtech.blaze.admodel.client.entity.MatchEntities;
import com.hotstar.adtech.blaze.admodel.client.model.AdInfo;
import com.hotstar.adtech.blaze.admodel.client.model.GoalMatchInfo;
import com.hotstar.adtech.blaze.admodel.common.domain.ApiErrorResponse;
import com.hotstar.adtech.blaze.admodel.common.enums.CampaignStatus;
import com.hotstar.adtech.blaze.admodel.common.enums.DeliveryMode;
import com.hotstar.adtech.blaze.admodel.common.exception.ApiErrorException;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import com.hotstar.adtech.blaze.exchanger.api.response.AdModelResultUriResponse;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.Ad;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.AdModel;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.AdModelVersion;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.AdSet;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.Match;
import com.hotstar.adtech.blaze.reach.synchronizer.metric.MetricNames;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
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
  private final AdModelClient adModelClient;
  private final DataExchangerClient dataExchangerClient;
  private final AtomicReference<AdModel> adModelAtomicReference = new AtomicReference<>();
  private final AtomicBoolean adModelReady = new AtomicBoolean(false);

  @PostConstruct
  public void init() {
    adModelAtomicReference.set(
      AdModel.builder()
        .matches(Collections.emptyList())
        .adMap(Collections.emptyMap())
        .adModelVersion(AdModelVersion.builder().version(-1L)
          .liveMatchMd5("")
          .adModelMd5("")
          .build())
        .build());

    loadAdModel();
  }

  @Scheduled(fixedDelayString = "${blaze.ad-reach-synchronizer.load.ad-model:10000}")
  @Timed(MetricNames.LOAD_AD_MODEL)
  public void loadAdModel() {
    AdModelVersion adModelVersion = get().getAdModelVersion();

    try {
      AdModelResultUriResponse adModelResultUriResponse =
        dataExchangerClient.getLatestAdModel(adModelVersion.getVersion());
      if (adModelResultUriResponse != null) {
        String curAdModelMd5 = adModelResultUriResponse.getMd5(Names.Live_Ad_Model_PB);
        String curLiveMatchMd5 = adModelResultUriResponse.getMd5(Names.Match_PB);
        if (Objects.equals(curAdModelMd5, adModelVersion.getAdModelMd5())
          && Objects.equals(curLiveMatchMd5, adModelVersion.getLiveMatchMd5())) {
          LoadStatus.IGNORE.counter().increment();
          return;
        }
        AdModelUri adModelUri = buildAdModelUri(adModelResultUriResponse);

        MatchEntities matchEntities = adModelClient.loadMatch(adModelUri);
        List<Match> liveMatches = matchEntities.getMatches().stream().map(
          matchInfo -> new Match(matchInfo.getSiMatchId(), matchInfo.getContentId())
        ).collect(Collectors.toList());

        LiveEntities liveEntities = adModelClient.loadLiveAdModel(adModelUri);
        Map<String, Ad> adMap = liveEntities.getAds().stream().collect(
          Collectors.toMap(AdInfo::getCreativeId, this::buildAd));

        Map<String, List<AdSet>> adSetGroup = buildAdSets(liveEntities.getGoalMatches()).stream().collect(
          Collectors.groupingBy(AdSet::getContentId));

        AdModel adModel = AdModel.builder()
          .matches(liveMatches)
          .adMap(adMap)
          .adSetGroup(adSetGroup)
          .adModelVersion(AdModelVersion.builder().version(adModelUri.getVersion())
            .adModelMd5(curAdModelMd5)
            .liveMatchMd5(curLiveMatchMd5)
            .build())
          .build();
        adModelAtomicReference.set(adModel);
        adModelReady.set(true);

        LoadStatus.SUCCESS.counter().increment();
      } else {
        LoadStatus.URL_NULL.counter().increment();
      }
    } catch (Exception ex) {
      if (ex instanceof ApiErrorException) {
        ApiErrorResponse apiErrorResponse = ((ApiErrorException) ex).getApiErrorResponse();
        log.error("code: " + apiErrorResponse.getCode() + "; message: " + apiErrorResponse.getMessage());
      }
      LoadStatus.FAILED.counter().increment();
      log.error("Load Live Match Model failed", ex);
    }
  }

  public AdModel get() {
    return adModelAtomicReference.get();
  }

  private AdModelUri buildAdModelUri(AdModelResultUriResponse adModelResultUriResponse) {
    return AdModelUri.builder()
      .id(adModelResultUriResponse.getId())
      .path(adModelResultUriResponse.getPath())
      .version(adModelResultUriResponse.getVersion())
      .build();
  }

  private Ad buildAd(AdInfo adInfo) {
    return Ad.builder()
      .id(adInfo.getId())
      .creativeId(adInfo.getCreativeId())
      .adSetId(adInfo.getAdSetId())
      .creativeType(adInfo.getCreativeType())
      .build();
  }

  private List<AdSet> buildAdSets(List<GoalMatchInfo> adSets) {
    return adSets.stream()
      .filter(goalMatchInfo -> goalMatchInfo.getCampaignStatus() != CampaignStatus.PAUSED)
      .filter(GoalMatchInfo::isEnabled)
      .filter(goalMatchInfo -> goalMatchInfo.getDeliveryMode() == DeliveryMode.SSAI
        || goalMatchInfo.getDeliveryMode() == DeliveryMode.SSAI_SPOT)
      .map(this::buildAdSet)
      .collect(Collectors.toList());
  }

  private AdSet buildAdSet(GoalMatchInfo adSet) {
    return AdSet.builder()
      .contentId(adSet.getContentId())
      .campaignId(adSet.getCampaignId())
      .id(adSet.getAdSetId())
      .maximiseReach(adSet.isMaximiseReach())
      .build();
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
