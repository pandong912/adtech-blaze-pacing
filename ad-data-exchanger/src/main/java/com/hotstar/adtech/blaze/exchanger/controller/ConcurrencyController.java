package com.hotstar.adtech.blaze.exchanger.controller;

import static com.hotstar.adtech.blaze.exchanger.api.Constant.API_VERSION;
import static com.hotstar.adtech.blaze.exchanger.api.Constant.CONCURRENCY_PATH;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentCohortConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.service.ConcurrencyService;
import com.hotstar.adtech.blaze.exchanger.util.PlayoutIdValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CONCURRENCY_PATH + API_VERSION)
@RequiredArgsConstructor
public class ConcurrencyController {
  private final ConcurrencyService concurrencyService;

  @GetMapping("/content/{contentId}/cohort")
  public StandardResponse<List<ContentCohortConcurrencyResponse>> getContentCohortWiseConcurrency(
    @PathVariable String contentId) {
    List<ContentCohortConcurrencyResponse> contentCohortConcurrencyResponse =
      concurrencyService.getContentCohortWiseConcurrency(contentId);
    return StandardResponse.success(contentCohortConcurrencyResponse);
  }

  @GetMapping("/content/{contentId}/stream")
  public StandardResponse<List<ContentStreamConcurrencyResponse>> getContentStreamWiseConcurrency(
    @PathVariable String contentId) {
    List<ContentStreamConcurrencyResponse> contentStreamConcurrencyResponse =
      concurrencyService.getContentStreamWiseConcurrency(contentId);
    return StandardResponse.success(contentStreamConcurrencyResponse);
  }

  @GetMapping("/content/{contentId}/single-stream")
  public StandardResponse<Long> getContentStreamConcurrencyWithPlayoutId(@PathVariable String contentId,
                                                                         @RequestParam String playoutId) {
    PlayoutIdValidator.validate(playoutId);
    Long concurrency = concurrencyService.getContentStreamConcurrencyWithPlayoutId(contentId, playoutId);
    return StandardResponse.success(concurrency);
  }
}
