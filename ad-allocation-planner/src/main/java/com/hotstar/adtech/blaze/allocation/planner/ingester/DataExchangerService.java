package com.hotstar.adtech.blaze.allocation.planner.ingester;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_IMPRESSION_FETCH;
import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_REACH_FETCH;
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
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.DegradationReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.RedisReachStorage;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import com.hotstar.adtech.blaze.exchanger.api.entity.BreakId;
import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDefinition;
import com.hotstar.adtech.blaze.exchanger.api.entity.UnReachData;
import com.hotstar.adtech.blaze.exchanger.api.response.AdModelResultUriResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdSetImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakListResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakTypeResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentCohortConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.MatchProgressModelResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.UnReachResponse;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataExchangerService {
  private static final String DEFAULT_SSAI_TAG = "SSAI::";
  private static final String SPOT_BREAK = "Spot";
  public static final int SHARD = 50;
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

  public Map<String, StreamDefinition> getStreamDefinition(String contentId) {
    List<StreamDefinition> streamDefinitions = Optional.ofNullable(dataExchangerClient.getStreamDefinitionV2(contentId))
      .filter(RespUtil::isSuccess)
      .map(StandardResponse::getData)
      .orElseThrow(() -> new ServiceException("Failed to get stream definition from data exchanger"));

    return streamDefinitions.stream()
      .collect(Collectors.toMap(StreamDefinition::getPlayoutId, Function.identity()));
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

  public List<ContentCohort> getContentCohortConcurrency(String contentId,
                                                         Map<String, StreamDefinition> streamDefinitionMap) {
    StandardResponse<List<ContentCohortConcurrencyResponse>> response =
      dataExchangerClient.getContentCohortWiseConcurrency(contentId);

    if (!Objects.equals(response.getCode(), ResultCode.SUCCESS)) {
      throw new ServiceException(response.getMessage());
    }

    return response.getData()
      .stream()
      .map(contentCohortConcurrencyResponse -> getContentCohort(contentId, streamDefinitionMap,
        contentCohortConcurrencyResponse))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private ContentCohort getContentCohort(String contentId, Map<String, StreamDefinition> streamDefinitionMap,
                                         ContentCohortConcurrencyResponse resp) {
    StreamDefinition streamDefinition = streamDefinitionMap.get(resp.getPlayoutId());
    if (streamDefinition == null) {
      Metrics.counter("blaze.stream.definition.notfound", "content", contentId).increment();
      return null;
    }
    return ContentCohort.builder()
      .contentId(contentId)
      .ssaiTag(getSsaiTag(resp.getSsaiTag()))
      .playoutStream(buildPlayoutStream(streamDefinition))
      .concurrency(resp.getConcurrencyValue())
      .playoutId(resp.getPlayoutId())
      .streamType(streamDefinition.getStreamType())
      .build();
  }

  public List<ContentStream> getContentStreamConcurrency(String contentId,
                                                         Map<String, StreamDefinition> streamDefinitionMap) {
    StandardResponse<List<ContentStreamConcurrencyResponse>> response =
      dataExchangerClient.getContentStreamWiseConcurrency(contentId);

    if (!Objects.equals(response.getCode(), ResultCode.SUCCESS)) {
      throw new ServiceException(response.getMessage());
    }

    return response.getData().stream()
      .map(contentStreamConcurrencyResponse -> getContentStream(contentId, streamDefinitionMap,
        contentStreamConcurrencyResponse))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private ContentStream getContentStream(String contentId, Map<String, StreamDefinition> streamDefinitionMap,
                                         ContentStreamConcurrencyResponse resp) {
    StreamDefinition streamDefinition = streamDefinitionMap.get(resp.getPlayoutId());
    if (streamDefinition == null) {
      Metrics.counter("blaze.stream.definition.notfound", "content", contentId).increment();
      return null;
    }
    return ContentStream.builder()
      .contentId(contentId)
      .playoutStream(buildPlayoutStream(streamDefinition))
      .playoutId(resp.getPlayoutId())
      .concurrency(resp.getConcurrencyValue())
      .streamType(streamDefinition.getStreamType())
      .build();
  }

  private PlayoutStream buildPlayoutStream(StreamDefinition streamDefinition) {
    return PlayoutStream.builder()
      .tenant(streamDefinition.getTenant())
      .language(streamDefinition.getLanguage())
      .platforms(streamDefinition.getPlatforms())
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

  @Timed(MATCH_REACH_FETCH)
  public ReachStorage getUnReachRatio(String contentId, Map<String, Integer> concurrencyIdMap,
                                      Map<Long, Integer> adSetIdToDemandId) {
    if (adSetIdToDemandId.size() == 0) {
      return new DegradationReachStorage();
    }
    List<UnReachResponse> unReachResponses = fetchUnReachData(contentId);
    int supplySize = concurrencyIdMap.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    double[][] unReachStore = new double[adSetIdToDemandId.size()][supplySize];

    for (double[] row : unReachStore) {
      Arrays.fill(row, 1.0);
    }

    for (UnReachResponse unReachResponse : unReachResponses) {
      Integer supplyId = concurrencyIdMap.get(unReachResponse.getKey());
      if (supplyId == null) {
        continue;
      }
      for (UnReachData unReachData : unReachResponse.getUnReachDataList()) {
        Integer demandId = adSetIdToDemandId.get(unReachData.getAdSetId());
        if (demandId == null) {
          continue;
        }
        unReachStore[demandId][supplyId] = unReachData.getUnReachRatio();
      }
    }

    doStatistics(unReachStore, contentId);
    return new RedisReachStorage(unReachStore);
  }

  private void doStatistics(double[][] unReachStore, String contentId) {
    log.info("{}: unReachStore size is {} * {}", contentId, unReachStore.length, unReachStore[0].length);
    int totalCount = 0;
    for (int supply = 0; supply < unReachStore[0].length; supply++) {
      for (double[] doubles : unReachStore) {
        if (doubles[supply] < 1.0) {
          totalCount++;
        }
      }
    }
    log.info("{}: percentage of unReachRatio smaller than 1 is {}", contentId,
      (double) totalCount / (unReachStore.length * unReachStore[0].length));
  }

  private List<UnReachResponse> fetchUnReachData(String contentId) {
    return IntStream.range(0, SHARD)
      .mapToObj(shard -> dataExchangerClient.batchGetUnReachDataInShard(contentId, shard))
      .filter(response -> filterResponse(response, contentId))
      .map(StandardResponse::getData)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  private boolean filterResponse(StandardResponse<List<UnReachResponse>> response, String contentId) {
    if (!RespUtil.isSuccess(response)) {
      log.error("fail to get reach data for contentId: {}, message: {}", contentId, response.getMessage());
      return false;
    }
    return true;
  }
}
