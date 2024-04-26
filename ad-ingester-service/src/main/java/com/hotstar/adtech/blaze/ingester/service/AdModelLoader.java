package com.hotstar.adtech.blaze.ingester.service;

import com.hotstar.adtech.blaze.admodel.client.common.Names;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import com.hotstar.adtech.blaze.exchanger.api.response.admodel.AdModelForIngesterResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.admodel.MatchResponse;
import com.hotstar.adtech.blaze.ingester.entity.Ad;
import com.hotstar.adtech.blaze.ingester.entity.AdModel;
import com.hotstar.adtech.blaze.ingester.entity.AdModelVersion;
import com.hotstar.adtech.blaze.ingester.entity.Match;
import com.hotstar.adtech.blaze.ingester.metric.MetricNames;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
  private final DataExchangerClient dataExchangerClient;
  private final AtomicReference<AdModel> adModelAtomicReference = new AtomicReference<>();
  private final AtomicBoolean adModelReady = new AtomicBoolean(false);

  @PostConstruct
  public void init() {
    loadAdModel();
  }

  @Scheduled(fixedDelayString = "${blaze.ad-ingester-service.load.ad-model:10000}")
  @Timed(MetricNames.LOAD_AD_MODEL)
  public void loadAdModel() {
    try {
      AdModelForIngesterResponse adModelForIngesterResponse = dataExchangerClient.getLatestAdModelForIngester();
      AdModel adModel = AdModel.builder()
        .matches(buildMatches(adModelForIngesterResponse))
        .streamMappingConverterGroup(adModelForIngesterResponse.getStreamMappingConverterGroup())
        .globalStreamMappingConverter(adModelForIngesterResponse.getGlobalStreamMappingConverter())
        .adMap(buildAdMap(adModelForIngesterResponse))
        .adModelVersion(AdModelVersion.builder()
          .version(adModelForIngesterResponse.getAdModelVersion().getVersionTimestamp())
          .adEntityMd5(adModelForIngesterResponse.getAdModelVersion().getFilenameToMd5().get(Names.AD_ENTITY_PB))
          .liveMatchMd5(adModelForIngesterResponse.getAdModelVersion().getFilenameToMd5().get(Names.LIVE_ENTITY_PB))
          .build())
        .build();
      adModelAtomicReference.set(adModel);
      adModelReady.set(true);

      LoadStatus.SUCCESS.counter().increment();
    } catch (Exception ex) {
      LoadStatus.FAILED.counter().increment();
      log.error("Load Live Match Model failed", ex);
    }
  }

  private List<Match> buildMatches(AdModelForIngesterResponse adModelForIngesterResponse) {
    return adModelForIngesterResponse
      .getMatches()
      .stream()
      .map(this::buildMatch)
      .collect(Collectors.toList());
  }

  private static Map<String, Ad> buildAdMap(AdModelForIngesterResponse adModelForIngesterResponse) {
    return adModelForIngesterResponse
      .getCreativeIdToAd()
      .entrySet()
      .stream()
      .collect(Collectors.toMap(Map.Entry::getKey, entry -> Ad.builder()
        .creativeType(entry.getValue().getCreativeType())
        .creativeId(entry.getValue().getCreativeId())
        .adSetId(entry.getValue().getAdSetId())
        .creativeType(entry.getValue().getCreativeType())
        .id(entry.getValue().getId())
        .build()));
  }

  private Match buildMatch(MatchResponse matchResponse) {
    return Match.builder()
      .tournamentId(matchResponse.getTournamentId())
      .contentId(matchResponse.getContentId())
      .seasonId(matchResponse.getSeasonId())
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
