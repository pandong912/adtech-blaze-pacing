package com.hotstar.adtech.blaze.allocation.planner.ingester;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_CONCURRENCY_FETCH;
import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_IMPRESSION_FETCH;
import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_TOTAL_BREAK_FETCH;

import com.hotstar.adtech.blaze.admodel.client.common.Names;
import com.hotstar.adtech.blaze.admodel.common.domain.ResultCode;
import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.admodel.common.util.RespUtil;
import com.hotstar.adtech.blaze.allocation.planner.common.model.AdModelVersion;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import com.hotstar.adtech.blaze.exchanger.api.entity.BreakId;
import com.hotstar.adtech.blaze.exchanger.api.response.AdModelResultUriResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdSetImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakListResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakTypeResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentCohortConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.MatchProgressModelResponse;
import io.micrometer.core.annotation.Timed;
import java.util.ArrayList;
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
  private static final String SPOT_BREAK = "Spot";

  private final DataExchangerClient dataExchangerClient;

  public AdModelVersion getLatestAdModelVersion(AdModelVersion adModelVersion) {
    return Optional.ofNullable(dataExchangerClient.getLatestAdModel(adModelVersion.getVersion()))
      .filter(RespUtil::isSuccess)
      .map(StandardResponse::getData)
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
    StandardResponse<MatchProgressModelResponse> response =
      dataExchangerClient.getLatestMatchBreakProgressModel();

    if (response.getCode() != ResultCode.SUCCESS) {
      throw new ServiceException("Failed to get break progress from data exchanger");
    }

    return response.getData().getDeliveryProgresses();
  }

  public List<BreakDetail> getBreakDefinition() {
    StandardResponse<List<BreakTypeResponse>> response = dataExchangerClient.getAllBreakType();

    if (response.getCode() != ResultCode.SUCCESS) {
      throw new ServiceException("Failed to get break type from data exchanger");
    }

    return response.getData().stream()
      .filter(breakTypeResponse -> SPOT_BREAK.equals(breakTypeResponse.getType()))
      .map(this::buildBreakDetail)
      .collect(Collectors.toList());
  }

  public Map<String, List<BreakId>> getBreakList(String contentId) {
    StandardResponse<List<BreakListResponse>> response = dataExchangerClient.getBreakList(contentId);

    if (response.getCode() != ResultCode.SUCCESS) {
      throw new ServiceException("Failed to get break list from data exchanger");
    }

    return response.getData().stream()
      .collect(Collectors.toMap(BreakListResponse::getPlayoutId, BreakListResponse::getBreakIds));
  }

  @Timed(MATCH_TOTAL_BREAK_FETCH)
  public Integer getTotalBreakNumber(String contentId) {
    StandardResponse<Integer> response = dataExchangerClient.getTotalBreakNumber(contentId);

    if (response.getCode() != ResultCode.SUCCESS) {
      throw new ServiceException("Failed to get break number from data exchanger");
    }

    return response.getData();
  }

  @Timed(MATCH_IMPRESSION_FETCH)
  public Map<Long, Long> getAdSetImpression(String contentId) {
    StandardResponse<List<AdSetImpressionResponse>> response = dataExchangerClient.getAllAdSetImpressions(contentId);

    if (!Objects.equals(response.getCode(), ResultCode.SUCCESS)) {
      throw new ServiceException(response.getMessage());
    }

    return response.getData().stream()
      .collect(Collectors.toMap(AdSetImpressionResponse::getAdSetId,
        AdSetImpressionResponse::getImpression));
  }

  @Timed(MATCH_CONCURRENCY_FETCH)
  public List<ContentCohort> getContentCohortConcurrency(String contentId,
                                                         Map<String, PlayoutStream> playoutStreamMap) {
    StandardResponse<List<ContentCohortConcurrencyResponse>> response =
      dataExchangerClient.getContentCohortWiseConcurrency(contentId);

    if (!Objects.equals(response.getCode(), ResultCode.SUCCESS)) {
      throw new ServiceException(response.getMessage());
    }

    return response.getData()
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
      log.error("playout stream can't be found, playoutId: {}", resp.getPlayoutId());
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
    StandardResponse<List<ContentStreamConcurrencyResponse>> response =
      dataExchangerClient.getContentStreamWiseConcurrency(contentId);

    if (!Objects.equals(response.getCode(), ResultCode.SUCCESS)) {
      throw new ServiceException(response.getMessage());
    }

    return response.getData().stream()
      .map(contentStreamConcurrencyResponse -> getContentStream(contentId, playoutStreamMap,
        contentStreamConcurrencyResponse))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private ContentStream getContentStream(String contentId, Map<String, PlayoutStream> playoutStreamMap,
                                         ContentStreamConcurrencyResponse resp) {
    PlayoutStream playoutStream = playoutStreamMap.get(resp.getPlayoutId());
    if (playoutStream == null) {
      log.error("playout stream can't be found, playoutId: {}", resp.getPlayoutId());
      return null;
    }
    return ContentStream.builder()
      .contentId(contentId)
      .playoutStream(playoutStream)
      .concurrency(resp.getConcurrencyValue())
      .build();
  }

  private BreakDetail buildBreakDetail(BreakTypeResponse breakTypeResponse) {
    List<Integer> durationList = new ArrayList<>();
    int duration = breakTypeResponse.getDurationLowerBound();
    while (duration < breakTypeResponse.getDurationUpperBound()) {
      durationList.add(duration);
      duration += breakTypeResponse.getStep();
    }
    durationList.add(breakTypeResponse.getDurationUpperBound());
    return BreakDetail.builder()
      .breakTypeId(breakTypeResponse.getId())
      .breakType(breakTypeResponse.getName())
      .breakDuration(durationList)
      .build();
  }

  private String getSsaiTag(String ssaiTag) {
    return ssaiTag.length() < 6 ? DEFAULT_SSAI_TAG : ssaiTag;
  }

}
