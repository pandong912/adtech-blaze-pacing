package com.hotstar.adtech.blaze.allocation.diagnosis.service;

import static com.hotstar.adtech.blaze.allocation.diagnosis.metric.MetricNames.AD_MODEL_AD;

import com.hotstar.adtech.blaze.admodel.client.model.AdInfo;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AdModelAd;
import com.hotstar.adtech.blaze.allocation.diagnosis.sink.AdModelAdSink;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdModelAdService {
  private final AdModelAdSink adModelAdSink;

  @Timed(AD_MODEL_AD)
  public void writeAd(List<AdInfo> adInfos, Instant version) {
    List<AdModelAd> ads = adInfos.stream()
      .map(ad -> AdModelAd.builder()
        .adSetId(ad.getAdSetId())
        .id(ad.getId())
        .adId(ad.getCreativeId())
        .enabled(ad.isEnabled())
        .version(version)
        .adType(ad.getCreativeType().toString())
        .duration(ad.getDurationMs())
        .languageIds(new ArrayList<>(ad.getLanguageIds()))
        .build()
      ).collect(Collectors.toList());
    adModelAdSink.write(ads);
  }
}
