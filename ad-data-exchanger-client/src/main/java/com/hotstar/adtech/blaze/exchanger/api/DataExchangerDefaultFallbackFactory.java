package com.hotstar.adtech.blaze.exchanger.api;

import com.hotstar.adtech.blaze.admodel.common.domain.ResultCode;
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
import feign.hystrix.FallbackFactory;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

public class DataExchangerDefaultFallbackFactory implements FallbackFactory<DataExchangerClient> {

  private final StandardResponse response = StandardResponse.builder()
    .code(ResultCode.FAILURE).message("Feign fallback").build();

  @Override
  public DataExchangerClient create(Throwable cause) {
    return new DataExchangerClient() {

      @Override
      public StandardResponse<List<BreakListResponse>> getBreakList(String contentId) {
        return response;
      }

      @Override
      public StandardResponse<BreakListResponse> getBreakListByStream(String contentId, String playoutId) {
        return response;
      }

      @Override
      public StandardResponse<Integer> getTotalBreakNumber(String contentId) {
        return response;
      }

      @Override
      public StandardResponse<List<BreakTypeResponse>> getAllBreakType() {
        return response;
      }

      @Override
      public StandardResponse<List<StreamDefinition>> getStreamDefinitionV2(String contentId) {
        return response;
      }

      @Override
      public StandardResponse<MatchProgressModelResponse> getMatchBreakProgressModel(
          @RequestParam String date) {
        return response;
      }

      @Override
      public StandardResponse<MatchProgressModelResponse> getLatestMatchBreakProgressModel() {
        return response;
      }

      @Override
      public StandardResponse<AdCrashModelResponse> getAdCrashModel(@RequestParam String date) {
        return response;
      }

      @Override
      public StandardResponse<AdCrashModelResponse> getLatestAdCrashModel() {
        return response;
      }

      @Override
      public StandardResponse<AllocationPlanUriResponse> getAllocationPlanUri(String contentId, Long version) {
        return response;
      }

      @Override
      public StandardResponse<List<ContentCohortConcurrencyResponse>> getContentCohortWiseConcurrency(
        String contentId) {
        return response;
      }

      @Override
      public StandardResponse<List<ContentStreamConcurrencyResponse>> getContentStreamWiseConcurrency(
        String contentId) {
        return response;
      }

      @Override
      public StandardResponse<AdModelResultUriResponse> getLatestAdModel(long version) {
        return response;
      }

      @Override
      public StandardResponse<AdModelResultUriResponse> getAdModel(long version) {
        return response;
      }

      @Override
      public StandardResponse<Long> getContentStreamConcurrencyWithPlayoutId(String contentId, String playoutId) {
        return response;
      }

      @Override
      public StandardResponse<List<AdSetImpressionResponse>> getAllAdSetImpressions(String contentId) {
        return response;
      }

      @Override
      public StandardResponse<AdSetImpressionResponse> getAdSetImpression(String contentId, Long adSetId) {
        return response;
      }

      @Override
      public StandardResponse<List<AdImpressionResponse>> getAllAdImpressions(String contentId) {
        return response;
      }

      @Override
      public StandardResponse<AdImpressionResponse> getAdImpression(String contentId, String creativeId) {
        return response;
      }

      @Override
      public StandardResponse<List<UnReachResponse>> batchGetUnReachData(String contentId) {
        return response;
      }

      @Override
      public StandardResponse<List<UnReachResponse>> batchGetUnReachDataInShard(String contentId, int shard) {
        return response;
      }

      @Override
      public StandardResponse<UnReachResponse> getUnReachDataWithStreamId(String contentId, String streamId,
                                                                          String ssaiTag) {
        return response;
      }

      @Override
      public StandardResponse<TournamentInfoResponse> getSeasonIdByContentId(String contentId) {
        return response;
      }

    };
  }
}
