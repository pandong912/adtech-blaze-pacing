package com.hotstar.adtech.blaze.exchanger.controller;

import static com.hotstar.adtech.blaze.exchanger.api.Constant.AD_CONTEXT_PATH;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakListResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakTypeResponse;
import com.hotstar.adtech.blaze.exchanger.service.BreakService;
import com.hotstar.adtech.blaze.exchanger.service.GlobalConfigService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AD_CONTEXT_PATH)
@RequiredArgsConstructor
public class AdContextController {
  private final BreakService breakService;
  private final GlobalConfigService globalConfigService;

  @GetMapping("/break-list/{contentId}")
  public StandardResponse<List<BreakListResponse>> getBreakList(@PathVariable("contentId") String contentId) {
    return StandardResponse.success(breakService.getBreakList(contentId));
  }

  @GetMapping("/break-list/{contentId}/stream/{playoutId}")
  public StandardResponse<BreakListResponse> getBreakListByStream(
    @PathVariable("contentId") String contentId,
    @PathVariable("playoutId") String playoutId) {
    return StandardResponse.success(breakService.getBreakListByStream(contentId, playoutId));
  }

  @GetMapping("/break-number/{contentId}")
  public StandardResponse<Integer> getTotalBreakNumber(@PathVariable("contentId") String contentId) {
    Integer breakNumber = breakService.getTotalBreakNumber(contentId);
    return StandardResponse.success(breakNumber);
  }

  @GetMapping("/break-type")
  public StandardResponse<List<BreakTypeResponse>> getAllBreakType() {
    List<BreakTypeResponse> response = breakService.getAllBreakType();
    return StandardResponse.success(response);
  }

  @GetMapping("/flink/heartbeat/sample")
  public StandardResponse<Double> getFlinkHeartbeatSample() {
    Double flinkSample = globalConfigService.getFlinkHeartbeatSample();
    return StandardResponse.success(flinkSample);
  }

  @GetMapping("/flink/tracker/sample")
  public StandardResponse<Double> getFlinkTrackerSample() {
    Double flinkSample = globalConfigService.getFlinkTrackerSample();
    return StandardResponse.success(flinkSample);
  }

}
