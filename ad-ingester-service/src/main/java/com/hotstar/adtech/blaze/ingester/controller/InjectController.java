package com.hotstar.adtech.blaze.ingester.controller;

import com.hotstar.adtech.blaze.ingester.entity.ConcurrencyGroup;
import com.hotstar.adtech.blaze.ingester.service.InjectConcurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inject/concurrency")
@ConditionalOnBean(InjectConcurrencyService.class)
public class InjectController {

  // only works when concurrency
  @Autowired(required = false)
  private InjectConcurrencyService provider;

  @Operation(description = "cohort stream")
  @GetMapping("/{category}")
  Set<String> injectedKeys(@PathVariable("category") String category) {
    return switch (category) {
      case "cohort" -> provider.injectedCohortKeys();
      case "stream" -> provider.injectedStreamKeys();
      default -> throw new IllegalStateException("Unexpected value: " + category);
    };
  }

  @Operation(description = "<P1|SSAI::xxx, 1>")
  @PostMapping("/cohort/{contentId}")
  public void injectCohort(@PathVariable("contentId") String contentId, @RequestBody Map<String, Long> concurrency) {
    provider.putCohort(contentId, concurrency);
  }

  @Operation(description = "<P1, 1>")
  @PostMapping("/stream/{contentId}")
  public void injectStream(@PathVariable("contentId") String contentId, @RequestBody Map<String, Long> concurrency) {
    provider.putStream(contentId, concurrency);
  }

  @DeleteMapping("/cohort/{contentId}")
  public void clearCohort(@PathVariable("contentId") String contentId) {
    provider.clearCohort(contentId);
  }

  @DeleteMapping("/stream/{contentId}")
  public void clearStream(@PathVariable("contentId") String contentId) {
    provider.clearStream(contentId);
  }

}
