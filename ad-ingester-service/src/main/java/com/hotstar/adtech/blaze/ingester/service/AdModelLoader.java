package com.hotstar.adtech.blaze.ingester.service;

import com.hotstar.adtech.blaze.admodel.client.AdModelClient;
import com.hotstar.adtech.blaze.admodel.client.AdModelUri;
import com.hotstar.adtech.blaze.admodel.client.common.Names;
import com.hotstar.adtech.blaze.admodel.client.entity.LiveEntities;
import com.hotstar.adtech.blaze.admodel.client.entity.MatchEntities;
import com.hotstar.adtech.blaze.admodel.client.model.AdInfo;
import com.hotstar.adtech.blaze.admodel.client.model.MatchInfo;
import com.hotstar.adtech.blaze.admodel.client.model.StreamMappingInfo;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerNewClient;
import com.hotstar.adtech.blaze.exchanger.api.response.AdModelResultUriResponse;
import com.hotstar.adtech.blaze.ingester.entity.Ad;
import com.hotstar.adtech.blaze.ingester.entity.AdModel;
import com.hotstar.adtech.blaze.ingester.entity.AdModelVersion;
import com.hotstar.adtech.blaze.ingester.entity.Match;
import com.hotstar.adtech.blaze.ingester.entity.SingleStream;
import com.hotstar.adtech.blaze.ingester.metric.MetricNames;
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
  private final DataExchangerNewClient dataExchangerNewClient;
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

  @Scheduled(fixedDelayString = "${blaze.ad-ingester-service.load.ad-model:10000}")
  @Timed(MetricNames.LOAD_AD_MODEL)
  public void loadAdModel() {
    AdModelVersion adModelVersion = get().getAdModelVersion();

    try {
      AdModelResultUriResponse adModelResultUriResponse =
        dataExchangerNewClient.getLatestAdModel(adModelVersion.getVersion());
      String curAdModelMd5 = adModelResultUriResponse.getMd5(Names.Live_Ad_Model_PB);
      String curLiveMatchMd5 = adModelResultUriResponse.getMd5(Names.Match_PB);
      if (Objects.equals(curAdModelMd5, adModelVersion.getAdModelMd5())
        && Objects.equals(curLiveMatchMd5, adModelVersion.getLiveMatchMd5())) {
        LoadStatus.IGNORE.counter().increment();
        return;
      }
      AdModelUri adModelUri = buildAdModelUri(adModelResultUriResponse);

      MatchEntities matchEntities = adModelClient.loadMatch(adModelUri);

      List<Match> liveMatches = matchEntities.getMatches().stream()
        .map(this::buildMatch)
        .collect(Collectors.toList());
      Map<Long, Map<String, String>> streamMappingConverterGroup = matchEntities.getStreamMappings().stream()
        .collect(Collectors.groupingBy(StreamMappingInfo::getSeasonId,
          Collectors.collectingAndThen(Collectors.toList(), this::buildStreamMappingConverter)));
      Map<String, String> globalStreamMappingConverter =
        buildStreamMappingConverter(matchEntities.getGlobalStreamMappings());

      LiveEntities liveEntities = adModelClient.loadLiveAdModel(adModelUri);
      Map<String, Ad> adMap = liveEntities.getAds().stream().collect(
        Collectors.toMap(AdInfo::getCreativeId, this::buildAd));

      AdModel adModel = AdModel.builder()
        .matches(liveMatches)
        .streamMappingConverterGroup(streamMappingConverterGroup)
        .globalStreamMappingConverter(globalStreamMappingConverter)
        .adMap(adMap)
        .adModelVersion(AdModelVersion.builder().version(adModelUri.getVersion())
          .adModelMd5(curAdModelMd5)
          .liveMatchMd5(curLiveMatchMd5)
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

  private Match buildMatch(MatchInfo matchInfo) {
    return Match.builder()
      .contentId(matchInfo.getContentId())
      .tournamentId(matchInfo.getTournamentId())
      .seasonId(matchInfo.getSeasonId())
      .build();
  }

  private Map<String, String> buildStreamMappingConverter(List<StreamMappingInfo> streamMappings) {
    return streamMappings.stream()
      .flatMap(streamMappingInfo -> buildStream(streamMappingInfo).stream())
      .collect(Collectors.toMap(SingleStream::getKey, SingleStream::getPlayoutId));
  }

  private List<SingleStream> buildStream(StreamMappingInfo streamMappingInfo) {
    return streamMappingInfo.getLadders().stream()
      .map(ladder ->
        SingleStream.builder()
          .tenant(streamMappingInfo.getTenant().getName())
          .language(streamMappingInfo.getLanguage().getAbbreviation())
          .ladder(ladder.name())
          .ads(streamMappingInfo.getStreamType().getAds())
          .playoutId(streamMappingInfo.getPlayoutId())
          .build()
      ).collect(Collectors.toList());
  }

  private Ad buildAd(AdInfo adInfo) {
    return Ad.builder()
      .id(adInfo.getId())
      .creativeId(adInfo.getCreativeId())
      .adSetId(adInfo.getAdSetId())
      .creativeType(adInfo.getCreativeType())
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
