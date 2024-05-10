package com.hotstar.adtech.blaze.ingester.controller;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.ingester.entity.AdModel;
import com.hotstar.adtech.blaze.ingester.service.AdModelLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

  private final AdModelLoader adModelLoader;

  @GetMapping(value = "ad-model")
  public StandardResponse<AdModelResponse> getAdModel() {
    AdModel adModel = adModelLoader.get();

    AdModelResponse adModelResponse = AdModelResponse.builder()
      .matches(adModel.getMatches())
      .streamMappingConverterGroup(adModel.getStreamMappingConverterGroup())
      .globalStreamMappingConverter(adModel.getGlobalStreamMappingConverter())
      .adModelVersion(adModel.getAdModelVersion())
      .build();

    return StandardResponse.success(adModelResponse);
  }

}
