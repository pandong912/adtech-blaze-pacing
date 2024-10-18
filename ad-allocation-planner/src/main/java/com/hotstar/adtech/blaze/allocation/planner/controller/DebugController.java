package com.hotstar.adtech.blaze.allocation.planner.controller;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("debug")
@Profile("!sim && !worker")
@RequiredArgsConstructor
public class DebugController {

  private final DataLoader dataLoader;

  @GetMapping("/ad-model")
  public AdModel getAdModel() {
    return dataLoader.getAdModel();
  }

}