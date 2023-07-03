package com.hotstar.adtech.blaze.exchanger.api;

import com.hotstar.adtech.blaze.admodel.common.domain.ResultCode;
import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.exchanger.api.entity.CohortInfo;
import com.hotstar.adtech.blaze.exchanger.api.response.AdImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdModelResultUriResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdSetImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AllocationPlanUriResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakListResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakTypeResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentCohortConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.UnReachResponse;
import feign.hystrix.FallbackFactory;
import java.util.List;

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
      public StandardResponse<ContentStreamResponse> getStreamDefinition(String contentId) {
        return response;
      }

      @Override
      public StandardResponse<List<Double>> getMatchBreakProgressModel(String date) {
        return response;
      }

      @Override
      public StandardResponse<List<Double>> getMatchBreakProgressModel() {
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
      public StandardResponse<Long> getContentSingleStreamConcurrency(String contentId, Tenant tenant,
                                                                      String language, String platform) {
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
      public StandardResponse<List<UnReachResponse>> batchGetUnReachData(String contentId,
                                                                         List<CohortInfo> cohortInfos) {
        return response;
      }

      @Override
      public StandardResponse<UnReachResponse> getUnReachDataWithStreamId(String contentId, String streamId,
                                                                          String ssaiTag) {
        return response;
      }

      @Override
      public StandardResponse<Long> getSeasonIdByContentId(String contentId) {
        return response;
      }

      @Override
      public StandardResponse<List<CohortInfo>> getReachCohortList(String contentId) {
        return response;
      }
    };
  }
}
