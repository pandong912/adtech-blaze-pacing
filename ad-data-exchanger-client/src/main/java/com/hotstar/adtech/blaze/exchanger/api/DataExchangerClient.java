package com.hotstar.adtech.blaze.exchanger.api;

import static com.hotstar.adtech.blaze.exchanger.api.Constant.AD_CONTEXT_PATH;
import static com.hotstar.adtech.blaze.exchanger.api.Constant.AD_MODEL_PATH;
import static com.hotstar.adtech.blaze.exchanger.api.Constant.ALGORITHM_PATH;
import static com.hotstar.adtech.blaze.exchanger.api.Constant.ALLOCATION_PLAN_PATH;
import static com.hotstar.adtech.blaze.exchanger.api.Constant.API_VERSION;
import static com.hotstar.adtech.blaze.exchanger.api.Constant.CONCURRENCY_PATH;
import static com.hotstar.adtech.blaze.exchanger.api.Constant.IMPRESSION_PATH;
import static com.hotstar.adtech.blaze.exchanger.api.Constant.REACH_PATH;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDefinition;
import com.hotstar.adtech.blaze.exchanger.api.response.AdCrashModelResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdModelResultUriResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdSetImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AllocationPlanUriResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakListResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakTypeResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentCohortConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.MatchProgressModelResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.TournamentInfoResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.UnReachResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = Constant.SERVICE_NAME,
  url = "${blaze.data-exchanger.endpoint}",
  fallbackFactory = DataExchangerDefaultFallbackFactory.class,
  configuration = FeignClientConfig.class
)
public interface DataExchangerClient {
  @GetMapping(AD_CONTEXT_PATH + "/break-list/{contentId}")
  StandardResponse<List<BreakListResponse>> getBreakList(
    @PathVariable("contentId") String contentId);

  @GetMapping(AD_CONTEXT_PATH + "/break-list/{contentId}/stream/{playoutId}")
  StandardResponse<BreakListResponse> getBreakListByStream(
    @PathVariable("contentId") String contentId,
    @PathVariable("playoutId") String playoutId);

  @GetMapping(AD_CONTEXT_PATH + "/break-number/{contentId}")
  StandardResponse<Integer> getTotalBreakNumber(
    @PathVariable("contentId") String contentId);

  @GetMapping(AD_CONTEXT_PATH + "/break-type")
  StandardResponse<List<BreakTypeResponse>> getAllBreakType();

  @GetMapping(AD_MODEL_PATH + "/stream-definition/v2/content/{contentId}")
  StandardResponse<List<StreamDefinition>> getStreamDefinitionV2(@PathVariable String contentId);

  @GetMapping(ALGORITHM_PATH + "/match-break-progress")
  StandardResponse<MatchProgressModelResponse> getMatchBreakProgressModel(
      @RequestParam String date);

  @GetMapping(ALGORITHM_PATH + "/match-break-progress/latest")
  StandardResponse<MatchProgressModelResponse> getLatestMatchBreakProgressModel();

  @GetMapping(ALGORITHM_PATH + "/ad-crash-distribution")
  StandardResponse<AdCrashModelResponse> getAdCrashModel(@RequestParam String date);

  @GetMapping(ALGORITHM_PATH + "/ad-crash-distribution/latest")
  StandardResponse<AdCrashModelResponse> getLatestAdCrashModel();

  @GetMapping(ALLOCATION_PLAN_PATH + "/match/{contentId}")
  StandardResponse<AllocationPlanUriResponse> getAllocationPlanUri(
    @PathVariable String contentId,
    @RequestParam Long version);

  @GetMapping(CONCURRENCY_PATH + API_VERSION + "/content/{contentId}/cohort")
  StandardResponse<List<ContentCohortConcurrencyResponse>> getContentCohortWiseConcurrency(
    @PathVariable String contentId);

  @GetMapping(CONCURRENCY_PATH + API_VERSION + "/content/{contentId}/stream")
  StandardResponse<List<ContentStreamConcurrencyResponse>> getContentStreamWiseConcurrency(
    @PathVariable String contentId);

  @GetMapping(AD_MODEL_PATH + "/ad-model-result/latest/{version}")
  StandardResponse<AdModelResultUriResponse> getLatestAdModel(@PathVariable long version);

  @GetMapping(AD_MODEL_PATH + "/ad-model-result/{version}")
  StandardResponse<AdModelResultUriResponse> getAdModel(@PathVariable long version);

  @GetMapping(CONCURRENCY_PATH + API_VERSION + "/content/{contentId}/single-stream")
  StandardResponse<Long> getContentStreamConcurrencyWithPlayoutId(@PathVariable String contentId,
                                                                  @RequestParam String playoutId);

  @GetMapping(REACH_PATH + "/content/{contentId}/reach/batch")
  StandardResponse<List<UnReachResponse>> batchGetUnReachData(@PathVariable String contentId);

  @GetMapping(REACH_PATH + "/content/{contentId}/reach/shard")
  StandardResponse<List<UnReachResponse>> batchGetUnReachDataInShard(@PathVariable String contentId,
                                                                     @RequestParam int shard);

  @GetMapping(REACH_PATH + "/content/{contentId}/reach")
  StandardResponse<UnReachResponse> getUnReachDataWithStreamId(@PathVariable String contentId,
                                                               @RequestParam String streamId,
                                                               @RequestParam String ssaiTag);

  @GetMapping(IMPRESSION_PATH + API_VERSION + "/content/{contentId}/ad-set/all")
  StandardResponse<List<AdSetImpressionResponse>> getAllAdSetImpressions(@PathVariable("contentId") String contentId);

  @GetMapping(IMPRESSION_PATH + API_VERSION + "/content/{contentId}/ad-set/{adSetId}")
  StandardResponse<AdSetImpressionResponse> getAdSetImpression(@PathVariable("contentId") String contentId,
                                                               @PathVariable("adSetId") Long adSetId);

  @GetMapping(IMPRESSION_PATH + API_VERSION + "/content/{contentId}/ad/all")
  StandardResponse<List<AdImpressionResponse>> getAllAdImpressions(@PathVariable("contentId") String contentId);

  @GetMapping(IMPRESSION_PATH + API_VERSION + "/content/{contentId}/ad/{creativeId}")
  StandardResponse<AdImpressionResponse> getAdImpression(@PathVariable("contentId") String contentId,
                                                         @PathVariable("creativeId") String creativeId);

  @GetMapping(AD_MODEL_PATH + "/season-id/content/{contentId}")
  StandardResponse<TournamentInfoResponse> getSeasonIdByContentId(@PathVariable String contentId);
}
