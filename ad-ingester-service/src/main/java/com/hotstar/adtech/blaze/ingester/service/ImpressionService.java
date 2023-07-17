package com.hotstar.adtech.blaze.ingester.service;

import com.hotstar.adtech.blaze.admodel.common.enums.CreativeCategory;
import com.hotstar.adtech.blaze.adserver.data.redis.service.ImpressionRepository;
import com.hotstar.adtech.blaze.ingester.entity.Ad;
import com.hotstar.adtech.blaze.ingester.entity.AdImpression;
import com.hotstar.adtech.blaze.ingester.entity.Match;
import com.hotstar.adtech.blaze.ingester.metric.MetricNames;
import com.hotstar.adtech.blaze.ingester.metric.MetricTags;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Deprecated
public class ImpressionService {

  private final PulseService pulseService;

  private final ImpressionRepository impressionRepository;

  @Async("impressionExecutor")
  @Timed(MetricNames.AD_SET_IMPRESSION_SYNC)
  public void updateImpression(Match match, Map<String, Ad> adMap) {
    try {
      String contentId = match.getContentId();

      List<AdImpression> matchAdsImpressions = pulseService.getMatchAdImpression(contentId);
      Map<String, Long> adImpressions = new HashMap<>();
      Map<String, Long> adSetImpressions = new HashMap<>();
      matchAdsImpressions.forEach(adImpression -> {
        Ad ad = adMap.get(adImpression.getCreativeId());
          if (ad != null) {
            Long impressionNumber = adImpression.getImpression();
            adImpressions.merge(ad.getCreativeId(), impressionNumber, Long::sum);
            if (isVideoAd(ad)) {
              adSetImpressions.merge(String.valueOf(ad.getAdSetId()), impressionNumber, Long::sum);
            }
          }
        }
      );

      //impressionRepository.setPulseAdImpressions(contentId, adImpressions);
      //impressionRepository.setPulseAdSetImpressions(contentId, adSetImpressions);
    } catch (Exception e) {
      Metrics.counter(MetricNames.IMPRESSION_UPDATE_EXCEPTION, MetricTags.EXCEPTION_CLASS, e.getClass().getName())
        .increment();
      log.error("Fail to update match impression, content id: " + match.getContentId(), e);
    }
  }

  private boolean isVideoAd(Ad ad) {
    return Objects.equals(CreativeCategory.Video, ad.getCreativeType().getCategory());
  }

}
