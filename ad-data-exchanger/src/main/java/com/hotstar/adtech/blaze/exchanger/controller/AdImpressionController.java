package com.hotstar.adtech.blaze.exchanger.controller;

import static com.hotstar.adtech.blaze.exchanger.api.Constant.API_VERSION;
import static com.hotstar.adtech.blaze.exchanger.api.Constant.IMPRESSION_PATH;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdSetImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.service.ImpressionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(IMPRESSION_PATH + API_VERSION)
@RequiredArgsConstructor
public class AdImpressionController {
  private final ImpressionService impressionService;

  @GetMapping("/content/{contentId}/ad-set/all")
  public StandardResponse<List<AdSetImpressionResponse>> getAllAdSetImpressions(
    @PathVariable("contentId") String contentId) {
    List<AdSetImpressionResponse> adSetImpressionResponse = impressionService.getAdSetImpression(contentId);
    return StandardResponse.success(adSetImpressionResponse);
  }

  @GetMapping("/content/{contentId}/ad-set/{adSetId}")
  public StandardResponse<AdSetImpressionResponse> getAdSetImpression(
    @PathVariable("contentId") String contentId,
    @PathVariable("adSetId") Long adSetId) {
    AdSetImpressionResponse adSetImpressionResponse = impressionService.getAdSetImpression(contentId, adSetId);
    return StandardResponse.success(adSetImpressionResponse);
  }

  @GetMapping("/content/{contentId}/ad/all")
  public StandardResponse<List<AdImpressionResponse>> getAllAdImpressions(
    @PathVariable("contentId") String contentId) {
    List<AdImpressionResponse> adSetImpressionResponse = impressionService.getAdImpression(contentId);
    return StandardResponse.success(adSetImpressionResponse);
  }

  @GetMapping("/content/{contentId}/ad/{creativeId}")
  public StandardResponse<AdImpressionResponse> getAdImpression(
    @PathVariable("contentId") String contentId,
    @PathVariable("creativeId") String creativeId) {
    AdImpressionResponse adImpressionResponse = impressionService.getAdImpression(contentId, creativeId);
    return StandardResponse.success(adImpressionResponse);
  }

}
