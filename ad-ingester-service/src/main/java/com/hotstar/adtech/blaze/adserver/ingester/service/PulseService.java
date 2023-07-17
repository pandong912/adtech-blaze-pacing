package com.hotstar.adtech.blaze.adserver.ingester.service;

import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.adserver.ingester.entity.AdImpression;
import com.hotstar.adtech.blaze.adserver.ingester.entity.ConcurrencyGroup;
import com.hotstar.platform.pulse.api.PulseClient;
import com.hotstar.platform.pulse.api.response.AdImpressionBatchResponse;
import com.hotstar.platform.pulse.api.response.ContentDataConcurrencyBatchResponse;
import com.hotstar.platform.pulse.entities.StandardResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PulseService {

  private final PulseClient pulseClient;

  public ConcurrencyGroup getLiveContentCohortConcurrency(String contentId) {

    StandardResponse<ContentDataConcurrencyBatchResponse> response =
      pulseClient.getLiveContentAllCohortConcurrency(contentId, null);

    if (!response.succeed()) {
      throw new ServiceException(response.getMessage());
    }

    ContentDataConcurrencyBatchResponse batchConcurrency = response.getData();

    return ConcurrencyGroup.builder()
      .tsBucket(batchConcurrency.getTsBucket())
      .concurrencyValues(batchConcurrency.getConcurrencyValues())
      .build();
  }

  public ConcurrencyGroup getLiveContentStreamCohortConcurrency(String contentId) {

    StandardResponse<ContentDataConcurrencyBatchResponse> response =
      pulseClient.getLiveContentAllCohortConcurrencyV3(contentId, null);

    if (!response.succeed()) {
      throw new ServiceException(response.getMessage());
    }

    ContentDataConcurrencyBatchResponse batchConcurrency = response.getData();

    return ConcurrencyGroup.builder()
      .tsBucket(batchConcurrency.getTsBucket())
      .concurrencyValues(batchConcurrency.getConcurrencyValues())
      .build();
  }

  public ConcurrencyGroup getLiveContentStreamConcurrency(String contentId) {

    StandardResponse<ContentDataConcurrencyBatchResponse> response =
      pulseClient.getLiveContentAllStreamConcurrencyV3(contentId, null);

    if (!response.succeed()) {
      throw new ServiceException(response.getMessage());
    }

    ContentDataConcurrencyBatchResponse batchConcurrency = response.getData();

    return ConcurrencyGroup.builder()
      .tsBucket(batchConcurrency.getTsBucket())
      .concurrencyValues(batchConcurrency.getConcurrencyValues())
      .build();
  }

  public List<AdImpression> getMatchAdImpression(String contentId) {
    StandardResponse<AdImpressionBatchResponse> response = pulseClient.getMatchAllAdImpression(contentId);

    if (!response.succeed()) {
      throw new ServiceException(response.getMessage());
    }

    return response.getData().getAdImpressions().entrySet().stream().map(entry ->
      AdImpression.builder()
        .creativeId(entry.getKey())
        .impression(entry.getValue())
        .build()).collect(Collectors.toList());
  }

}
