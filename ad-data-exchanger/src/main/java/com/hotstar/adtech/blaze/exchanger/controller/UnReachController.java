package com.hotstar.adtech.blaze.exchanger.controller;

import static com.hotstar.adtech.blaze.exchanger.api.Constant.REACH_PATH;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.exchanger.api.entity.CohortInfo;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.UnReachResponse;
import com.hotstar.adtech.blaze.exchanger.service.StreamService;
import com.hotstar.adtech.blaze.exchanger.service.UnReachService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(REACH_PATH)
@RequiredArgsConstructor
public class UnReachController {
  private final UnReachService unReachService;
  private final StreamService streamService;

  @PostMapping("/content/{contentId}/reach/batch")
  public StandardResponse<List<UnReachResponse>> batchGetUnReachData(@PathVariable String contentId,
                                                                     @RequestBody List<CohortInfo> cohortInfos) {
    List<UnReachResponse> unReachResponses = unReachService.batchGetCohortReach(contentId, cohortInfos);
    return StandardResponse.success(unReachResponses);
  }

  @GetMapping("/content/{contentId}/reach")
  public StandardResponse<UnReachResponse> getUnReachDataWithStreamId(@PathVariable String contentId,
                                                                      @RequestParam String streamId,
                                                                      @RequestParam String ssaiTag) {
    // todo: response time may be high
    ContentStreamResponse streamDefinition = streamService.getStreamDefinition(contentId);
    UnReachResponse unReachResponses = unReachService.getCohortReach(contentId, streamId, ssaiTag, streamDefinition);
    return StandardResponse.success(unReachResponses);
  }

  @GetMapping("/content/{contentId}/reach/cohort-list")
  public StandardResponse<List<CohortInfo>> getReachCohortList(@PathVariable String contentId) {
    List<CohortInfo> cohortList = unReachService.getCohortList(contentId);
    return StandardResponse.success(cohortList);
  }
}
