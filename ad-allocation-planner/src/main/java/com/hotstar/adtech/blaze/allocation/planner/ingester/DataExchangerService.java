package com.hotstar.adtech.blaze.allocation.planner.ingester;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_CONCURRENCY_FETCH;
import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_IMPRESSION_FETCH;
import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_TOTAL_BREAK_FETCH;

import com.hotstar.adtech.blaze.admodel.client.common.Names;
import com.hotstar.adtech.blaze.admodel.common.domain.ApiErrorResponse;
import com.hotstar.adtech.blaze.admodel.common.exception.ApiErrorException;
import com.hotstar.adtech.blaze.allocation.planner.common.model.AdModelVersion;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
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

  private final DataExchangerClient dataExchangerClient;

  public AdModelVersion getLatestAdModelVersion(AdModelVersion adModelVersion) {
    return Optional.ofNullable(dataExchangerClient.getLatestAdModel(adModelVersion.getVersion()))
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
    try {
      MatchProgressModelResponse response =
        dataExchangerClient.getLatestMatchBreakProgressModel();
      return response.getDeliveryProgresses();
    } catch (Exception ex) {
      handleApiErrorException(ex);
      log.error("Fail to get matchBreakProgressModel", ex);
      throw ex;
    }
  }

  public Map<String, List<BreakId>> getBreakList(String contentId) {
    try {
      List<BreakListResponse> response = dataExchangerClient.getBreakList(contentId);
      return response.stream()
        .collect(Collectors.toMap(BreakListResponse::getPlayoutId, BreakListResponse::getBreakIds));
    } catch (Exception ex) {
      handleApiErrorException(ex);
      log.error("Failed to get break list from data exchanger", ex);
      throw ex;
    }
  }

  @Timed(MATCH_TOTAL_BREAK_FETCH)
  public Integer getTotalBreakNumber(String contentId) {
    try {
      return dataExchangerClient.getTotalBreakNumber(contentId);
    } catch (Exception ex) {
      handleApiErrorException(ex);
      log.error("Failed to get totalBreakNumber", ex);
      throw ex;
    }
  }

  @Timed(MATCH_IMPRESSION_FETCH)
  public Map<Long, Long> getAdSetImpression(String contentId) {
    try {
      List<AdSetImpressionResponse> response = dataExchangerClient.getAllAdSetImpressions(contentId);
      return response.stream()
        .collect(Collectors.toMap(AdSetImpressionResponse::getAdSetId,
          AdSetImpressionResponse::getImpression));
    } catch (Exception ex) {
      handleApiErrorException(ex);
      log.error("Failed to get AdSetImpression", ex);
      throw ex;
    }
  }

  @Timed(MATCH_CONCURRENCY_FETCH)
  public List<ContentCohort> getContentCohortConcurrency(String contentId,
                                                         Map<String, PlayoutStream> playoutStreamMap) {
    try {
      List<ContentCohortConcurrencyResponse> response =
        dataExchangerClient.getContentCohortWiseConcurrency(contentId);
      return response
        .stream()
        .map(contentCohortConcurrencyResponse -> getContentCohort(contentId, playoutStreamMap,
          contentCohortConcurrencyResponse))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    } catch (Exception ex) {
      handleApiErrorException(ex);
      log.error("Failed to get ContentCohortConcurrency", ex);
      throw ex;
    }
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
    try {
      List<ContentStreamConcurrencyResponse> response =
        dataExchangerClient.getContentStreamWiseConcurrency(contentId);
      return response.stream()
        .map(contentStreamConcurrencyResponse -> getContentStream(contentId, playoutStreamMap,
          contentStreamConcurrencyResponse))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    } catch (Exception ex) {
      handleApiErrorException(ex);
      log.error("Failed to get ContentStreamConcurrency", ex);
      throw ex;
    }
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

  public void handleApiErrorException(Exception ex) {
    if (ex instanceof ApiErrorException) {
      ApiErrorResponse apiErrorResponse = ((ApiErrorException) ex).getApiErrorResponse();
      log.error("code: " + apiErrorResponse.getCode() + "; message: " + apiErrorResponse.getMessage());
    }
  }

}
