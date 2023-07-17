package com.hotstar.adtech.blaze.exchanger.controller;

import static com.hotstar.adtech.blaze.exchanger.api.Constant.ALGORITHM_PATH;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.exchanger.api.entity.Distribution;
import com.hotstar.adtech.blaze.exchanger.api.response.AdCrashModelResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.MatchProgressModelResponse;
import com.hotstar.adtech.blaze.exchanger.service.AlgorithmService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ALGORITHM_PATH)
@RequiredArgsConstructor
@Validated
public class AlgorithmController {

  private final AlgorithmService algorithmService;

  @GetMapping("/match-break-progress")
  public StandardResponse<MatchProgressModelResponse> getMatchBreakProgressModel(
      @RequestParam @NotEmpty String date) {
    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    List<Double> model = algorithmService.getMatchBreakProgressModel(localDate);

    return StandardResponse.success(buildMatchProgressModelResponse(model));
  }

  @GetMapping("/match-break-progress/latest")
  public StandardResponse<MatchProgressModelResponse> getLatestMatchBreakProgressModel() {
    List<Double> model = algorithmService.getLatestMatchBreakProgressModel();

    return StandardResponse.success(buildMatchProgressModelResponse(model));
  }

  @GetMapping("/ad-crash-distribution")
  public StandardResponse<AdCrashModelResponse> getAdCrashModel(
      @RequestParam @NotEmpty String date) {
    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    List<Distribution> model = algorithmService.getDistributionModel(localDate);

    return StandardResponse.success(buildAdCrashModelResponse(model));
  }

  @GetMapping("/ad-crash-distribution/latest")
  public StandardResponse<AdCrashModelResponse> getLatestAdCrashModel() {
    List<Distribution> model = algorithmService.getLatestDistributionModel();

    return StandardResponse.success(buildAdCrashModelResponse(model));
  }

  private static MatchProgressModelResponse buildMatchProgressModelResponse(List<Double> model) {
    return MatchProgressModelResponse.builder()
        .deliveryProgresses(model)
        .build();
  }

  private static AdCrashModelResponse buildAdCrashModelResponse(List<Distribution> model) {
    return AdCrashModelResponse.builder()
        .distributions(model)
        .build();
  }

}
