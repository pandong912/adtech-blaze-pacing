package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamView;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Language;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEvaluators;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.AspectRatioInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.DurationInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.LanguageInspector;
import com.hotstar.adtech.blaze.allocationdata.client.model.AdSetRemainImpr;
import java.util.BitSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StreamQualificationEngine {

  public BitSet qualify(PlayoutStream playoutStream, long concurrency,
                        Map<Integer, AdSetRemainImpr> candidateAdSets,
                        int relaxedDuration,
                        Integer breakTypeId, TargetingEvaluators evaluators) {
    BitSet qualified = (BitSet) evaluators.getActiveAdSetBitSet().clone();

    BitSet stream = evaluators.getStream().targeting(buildStreamKey(playoutStream));
    qualified.and(stream);

    BitSet breakTargeting = evaluators.getBreakTargeting().targeting(String.valueOf(breakTypeId));
    qualified.and(breakTargeting);

    BitSet aspectRatio = evaluators.getAspectRatio()
      .targeting(StreamView.fromLanguageName(playoutStream.getLanguage().getName()).toString());
    qualified.and(aspectRatio);

    BitSet duration =
      evaluators.getDuration().targeting(String.valueOf(evaluators.getDurationSet().floor(relaxedDuration)));
    qualified.and(duration);

    BitSet language = evaluators.getLanguage().targeting(String.valueOf(playoutStream.getLanguage().getId()));
    qualified.and(language);

    Predicate<Ad> adPredicate = buildAdInspector(relaxedDuration, playoutStream.getLanguage());
    Predicate<AdSetRemainImpr> adSetPredicate = buildAdSetInspector(concurrency);

    BitSet customTargeting = customTargeting(qualified, adPredicate, adSetPredicate, candidateAdSets);
    qualified.and(customTargeting);
    return qualified;
  }

  private Set<String> buildStreamKey(PlayoutStream playoutStream) {
    Language language = playoutStream.getLanguage();
    StreamType streamType = playoutStream.getStreamType();
    return playoutStream.getLadders()
      .stream()
      .map(ladder -> playoutStream.getTenant() + "+" + language.getId() + "+" + ladder + "+" + streamType)
      .collect(Collectors.toSet());
  }

  private BitSet customTargeting(BitSet preQualified, Predicate<Ad> adPredicate,
                                 Predicate<AdSetRemainImpr> adSetPredicate,
                                 Map<Integer, AdSetRemainImpr> candidateAdSets) {
    BitSet qualified = (BitSet) preQualified.clone();
    preQualified.stream()
      .mapToObj(candidateAdSets::get)
      .filter(adSetRemainImpr -> !adSetPredicate.test(adSetRemainImpr)
        || adSetRemainImpr.adSet().getSpotAds().stream().noneMatch(adPredicate))
      .map(AdSetRemainImpr::adSet)
      .map(AdSet::getDemandId)
      .forEach(qualified::clear);
    return qualified;
  }

  private Predicate<Ad> buildAdInspector(int relaxedDuration, Language language) {
    DurationInspector durationInspector = new DurationInspector(relaxedDuration);
    LanguageInspector languageInspector = new LanguageInspector(language.getId());
    AspectRatioInspector aspectRatioInspector = new AspectRatioInspector(language.getName());
    return ad -> durationInspector.qualify(ad) && languageInspector.qualify(ad) && aspectRatioInspector.qualify(ad);
  }

  private Predicate<AdSetRemainImpr> buildAdSetInspector(long concurrency) {
    if (concurrency < 2000) {
      return adSet -> true;
    }
    return adSet -> adSet.remainDelivery() > concurrency;
  }
}
