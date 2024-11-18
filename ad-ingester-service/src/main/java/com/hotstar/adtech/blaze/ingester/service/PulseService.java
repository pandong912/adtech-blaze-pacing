package com.hotstar.adtech.blaze.ingester.service;

import com.hotstar.adtech.blaze.admodel.common.exception.BusinessException;
import com.hotstar.adtech.blaze.ingester.entity.ConcurrencyGroup;
import com.hotstar.platform.pulse.api.PulseClient;
import com.hotstar.platform.pulse.api.response.ContentDataConcurrencyBatchResponse;
import com.hotstar.platform.pulse.entities.StandardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class PulseService {

  private final PulseClient pulseClient;

  public ConcurrencyGroup getLiveContentStreamCohortConcurrency(String contentId) {

    StandardResponse<ContentDataConcurrencyBatchResponse> response =
      pulseClient.getLiveContentAllCohortConcurrencyV3(contentId, null);

    if (!response.succeed()) {
      throw new BusinessException(response.getMessage());
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
      throw new BusinessException(response.getMessage());
    }

    ContentDataConcurrencyBatchResponse batchConcurrency = response.getData();

    return ConcurrencyGroup.builder()
      .tsBucket(batchConcurrency.getTsBucket())
      .concurrencyValues(batchConcurrency.getConcurrencyValues())
      .build();
  }

}
