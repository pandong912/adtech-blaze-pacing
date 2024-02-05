package com.hotstar.adtech.blaze.allocation.planner.ingester;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_CONCURRENCY_FETCH;
import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_IMPRESSION_FETCH;
import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_TOTAL_BREAK_FETCH;

import com.hotstar.adtech.blaze.admodel.client.common.Names;
import com.hotstar.adtech.blaze.allocation.planner.common.model.AdModelVersion;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerNewClient;
import com.hotstar.adtech.blaze.exchanger.api.entity.BreakId;
import com.hotstar.adtech.blaze.exchanger.api.response.AdModelResultUriResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdSetImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakListResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentCohortConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.MatchProgressModelResponse;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataExchangerService {
  private static final String DEFAULT_SSAI_TAG = "SSAI::";

  private final DataExchangerNewClient dataExchangerNewClient;

  public AdModelVersion getLatestAdModelVersion(AdModelVersion adModelVersion) {
    return Optional.ofNullable(dataExchangerNewClient.getLatestAdModel(adModelVersion.getVersion()))
      .map(this::buildAdModelVersion)
      .orElse(adModelVersion);
  }

  private AdModelVersion buildAdModelVersion(AdModelResultUriResponse adModelResultUriResponse) {
    return AdModelVersion.builder()
      .version(adModelResultUriResponse.getVersion())
      .id(adModelResultUriResponse.getId())
      .path(adModelResultUriResponse.getPath())
      .adModelMd5(adModelResultUriResponse.getMd5(Names.Live_Ad_Model_PB))
      .metadataMd5(adModelResultUriResponse.getMd5(Names.Meta_PB))
      .liveMatchMd5(adModelResultUriResponse.getMd5(Names.Match_PB))
      .build();
  }

  public List<Double> getMatchBreakProgressModel() {
    MatchProgressModelResponse response =
      dataExchangerNewClient.getLatestMatchBreakProgressModel();


    return response.getDeliveryProgresses();
  }


  public Map<String, List<BreakId>> getBreakList(String contentId) {
    List<BreakListResponse> response = dataExchangerNewClient.getBreakList(contentId);

    return response.stream()
      .collect(Collectors.toMap(BreakListResponse::getPlayoutId, BreakListResponse::getBreakIds));
  }

  @Timed(MATCH_TOTAL_BREAK_FETCH)
  public Integer getTotalBreakNumber(String contentId) {
    return dataExchangerNewClient.getTotalBreakNumber(contentId);
  }

  @Timed(MATCH_IMPRESSION_FETCH)
  public Map<Long, Long> getAdSetImpression(String contentId) {
    List<AdSetImpressionResponse> response = dataExchangerNewClient.getAllAdSetImpressions(contentId);

    return response.stream()
      .collect(Collectors.toMap(AdSetImpressionResponse::getAdSetId,
        AdSetImpressionResponse::getImpression));
  }

  @Timed(MATCH_CONCURRENCY_FETCH)
  public List<ContentCohort> getContentCohortConcurrency(String contentId,
                                                         Map<String, PlayoutStream> playoutStreamMap) {
    List<ContentCohortConcurrencyResponse> response =
      dataExchangerNewClient.getContentCohortWiseConcurrency(contentId);

    return response
      .stream()
      .map(contentCohortConcurrencyResponse -> getContentCohort(contentId, playoutStreamMap,
        contentCohortConcurrencyResponse))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private ContentCohort getContentCohort(String contentId, Map<String, PlayoutStream> playoutStreamMap,
                                         ContentCohortConcurrencyResponse resp) {
    PlayoutStream playoutStream = playoutStreamMap.get(resp.getPlayoutId());
    if (playoutStream == null) {
      log.warn("playout stream can't be found, playoutId: {}", resp.getPlayoutId());
      return null;
    }
    return ContentCohort.builder()
      .contentId(contentId)
      .ssaiTag(getSsaiTag(resp.getSsaiTag()))
      .playoutStream(playoutStream)
      .concurrency(resp.getConcurrencyValue())
      .build();
  }

  public List<ContentStream> getContentStreamConcurrency(String contentId,
                                                         Map<String, PlayoutStream> playoutStreamMap) {
    List<ContentStreamConcurrencyResponse> response =
      dataExchangerNewClient.getContentStreamWiseConcurrency(contentId);

    return response.stream()
      .map(contentStreamConcurrencyResponse -> getContentStream(contentId, playoutStreamMap,
        contentStreamConcurrencyResponse))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private ContentStream getContentStream(String contentId, Map<String, PlayoutStream> playoutStreamMap,
                                         ContentStreamConcurrencyResponse resp) {
    PlayoutStream playoutStream = playoutStreamMap.get(resp.getPlayoutId());
    if (playoutStream == null) {
      log.warn("playout stream can't be found, playoutId: {}", resp.getPlayoutId());
      return null;
    }
    return ContentStream.builder()
      .contentId(contentId)
      .playoutStream(playoutStream)
      .concurrency(resp.getConcurrencyValue())
      .build();
  }


  private String getSsaiTag(String ssaiTag) {
    return ssaiTag.length() < 6 ? DEFAULT_SSAI_TAG : ssaiTag;
  }

}
