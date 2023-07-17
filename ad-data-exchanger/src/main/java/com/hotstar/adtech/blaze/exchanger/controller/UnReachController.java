package com.hotstar.adtech.blaze.exchanger.controller;

import static com.hotstar.adtech.blaze.exchanger.api.Constant.REACH_PATH;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.UnReachResponse;
import com.hotstar.adtech.blaze.exchanger.service.UnReachService;
import com.hotstar.adtech.blaze.exchanger.util.PlayoutIdValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(REACH_PATH)
@RequiredArgsConstructor
public class UnReachController {
  private final UnReachService unReachService;

  @GetMapping("/content/{contentId}/reach/batch")
  public StandardResponse<List<UnReachResponse>> batchGetUnReachData(@PathVariable String contentId) {
    List<UnReachResponse> unReachResponses = unReachService.batchGetCohortReach(contentId);
    return StandardResponse.success(unReachResponses);
  }

  @GetMapping("/content/{contentId}/reach/shard")
  StandardResponse<List<UnReachResponse>> batchGetUnReachDataInShard(@PathVariable String contentId,
                                                                     @RequestParam int shard) {
    List<UnReachResponse> unReachResponses = unReachService.batchGetCohortReachInShard(contentId, shard);
    return StandardResponse.success(unReachResponses);
  }

  @GetMapping("/content/{contentId}/reach")
  public StandardResponse<UnReachResponse> getUnReachDataWithStreamId(@PathVariable String contentId,
                                                                      @RequestParam String playoutId,
                                                                      @RequestParam String ssaiTag) {
    PlayoutIdValidator.validate(playoutId);
    UnReachResponse unReachResponses = unReachService.getCohortReach(contentId, playoutId, ssaiTag);
    return StandardResponse.success(unReachResponses);
  }
}
