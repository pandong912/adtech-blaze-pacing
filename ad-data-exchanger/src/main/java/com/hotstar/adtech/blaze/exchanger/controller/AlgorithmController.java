package com.hotstar.adtech.blaze.exchanger.controller;

import static com.hotstar.adtech.blaze.exchanger.api.Constant.ALGORITHM_PATH;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.exchanger.service.AlgorithmService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ALGORITHM_PATH)
@RequiredArgsConstructor
public class AlgorithmController {

  private final AlgorithmService algorithmService;

  @GetMapping("/match-break-progress")
  public StandardResponse<List<Double>> getMatchBreakProgressModel(@RequestParam(required = false) String date) {
    if (StringUtils.isEmpty(date)) {
      return StandardResponse.success(algorithmService.getLatestMatchBreakProgressModel());
    }
    return StandardResponse.success(algorithmService.getMatchBreakProgressModel(date));
  }
}
