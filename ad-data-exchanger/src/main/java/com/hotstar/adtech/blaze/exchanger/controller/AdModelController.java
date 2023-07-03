package com.hotstar.adtech.blaze.exchanger.controller;

import static com.hotstar.adtech.blaze.exchanger.api.Constant.AD_MODEL_PATH;

import com.hotstar.adtech.blaze.admodel.common.domain.ResultCode;
import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdModelResultUriResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.TournamentInfoResponse;
import com.hotstar.adtech.blaze.exchanger.service.AdModelResultService;
import com.hotstar.adtech.blaze.exchanger.service.MatchService;
import com.hotstar.adtech.blaze.exchanger.service.StreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AD_MODEL_PATH)
@RequiredArgsConstructor
public class AdModelController {

  private final StreamService streamService;
  private final MatchService matchService;

  private final AdModelResultService adModelResultService;

  @GetMapping("/stream-definition/content/{contentId}")
  public StandardResponse<ContentStreamResponse> getStreamDefinition(@PathVariable String contentId) {
    ContentStreamResponse contentStreamResponse = streamService.getStreamDefinition(contentId);
    return StandardResponse.success(contentStreamResponse);
  }

  @GetMapping("/season-id/content/{contentId}")
  public StandardResponse<TournamentInfoResponse> getSeasonIdByContentId(@PathVariable String contentId) {
    return matchService.getSeasonIdByContentId(contentId)
      .map(StandardResponse::success)
      .orElseGet(() -> StandardResponse.error(ResultCode.FAILURE, "No SeasonId found for contentId: " + contentId));
  }

  @GetMapping("/ad-model-result/latest/{version}")
  public StandardResponse<AdModelResultUriResponse> getLatestAdModel(@PathVariable long version) {
    return adModelResultService.queryAdModelUriByVersionGreaterThan(version)
      .map(StandardResponse::success)
      .orElseGet(() -> StandardResponse.error(ResultCode.FAILURE,
        "No AdModelResult found for greater than version: " + version));
  }

  @GetMapping("/ad-model-result/{version}")
  public StandardResponse<AdModelResultUriResponse> getAdModel(@PathVariable long version) {
    return adModelResultService.queryAdModelUriByVersion(version)
      .map(StandardResponse::success)
      .orElseGet(() -> StandardResponse.error(ResultCode.FAILURE,
        "No AdModelResult found for version: " + version));
  }
}
