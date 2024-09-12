package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.util.BitSetUtil;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEvaluators;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.DurationInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.LanguageInspector;
import com.hotstar.adtech.blaze.allocationdata.client.model.AdSetRemainImpr;
import java.util.BitSet;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CohortQualificationEngine {


  public BitSet qualify(PlayoutStream playoutStream, long concurrency,
                        Map<Integer, AdSetRemainImpr> candidateAdSets,
                        int relaxedDuration,
                        Map<Integer, Set<String>> categoryId2Tags, TargetingEvaluators evaluators) {

    BitSet qualified = (BitSet) evaluators.getActiveAdSetBitSet().clone();

    BitSet stream = evaluators.getStream().targeting(buildStreamKey(playoutStream));
    qualified.and(stream);

    BitSet language = evaluators.getLanguage().targeting(String.valueOf(playoutStream.getLanguage().getId()));
    qualified.and(language);

    BitSet duration =
      evaluators.getDuration().targeting(String.valueOf(evaluators.getDurationSet().floor(relaxedDuration)));
    qualified.and(duration);

    BitSet audience = evaluators.getAudience().entrySet().stream()
      .map(e -> categoryTargeting(categoryId2Tags, e))
      .reduce(BitSetUtil.allTrue(evaluators.getAdSetSize()), BitSetUtil::and);
    qualified.and(audience);

    Predicate<Ad> adPredicate = buildAdInspector(relaxedDuration, playoutStream.getLanguage());
    Predicate<AdSetRemainImpr> adSetPredicate = buildAdSetInspector(concurrency);
    BitSet ad = adTargeting(qualified, adPredicate, adSetPredicate, candidateAdSets);
    qualified.and(ad);
    return qualified;
  }

  private static BitSet categoryTargeting(Map<Integer, Set<String>> categoryId2Tags,
                                          Map.Entry<Integer, TargetingEngine> e) {
    TargetingEngine categoryTargetingEngine = e.getValue();
    Set<String> tags = categoryId2Tags.getOrDefault(e.getKey(), Collections.emptySet());
    return categoryTargetingEngine.targeting(tags);
  }

  private Set<String> buildStreamKey(PlayoutStream playoutStream) {
    Language language = playoutStream.getLanguage();
    StreamType streamType = playoutStream.getStreamType();
    return playoutStream.getLadders()
      .stream()
      .map(ladder -> playoutStream.getTenant() + "+" + language.getId() + "+" + ladder + "+" + streamType)
      .collect(Collectors.toSet());
  }

  private BitSet adTargeting(BitSet preQualified, Predicate<Ad> adPredicate,
                             Predicate<AdSetRemainImpr> adSetPredicate,
                             Map<Integer, AdSetRemainImpr> candidateAdSets) {
    BitSet qualified = (BitSet) preQualified.clone();
    preQualified.stream()
      .mapToObj(candidateAdSets::get)
      .filter(adSetRemainImpr -> !adSetPredicate.test(adSetRemainImpr)
        || adSetRemainImpr.adSet().getSsaiAds().stream().noneMatch(adPredicate))
      .map(AdSetRemainImpr::adSet)
      .map(AdSet::getDemandId)
      .forEach(qualified::clear);
    return qualified;
  }

  private Predicate<Ad> buildAdInspector(int relaxedDuration, Language language) {
    DurationInspector durationInspector = new DurationInspector(relaxedDuration);
    LanguageInspector languageInspector = new LanguageInspector(language.getId());
    return ad -> durationInspector.qualify(ad) && languageInspector.qualify(ad);
  }

  private Predicate<AdSetRemainImpr> buildAdSetInspector(long concurrency) {
    if (concurrency < 2000) {
      return adSet -> true;
    }
    return adSet -> adSet.remainDelivery() > concurrency;
  }
}
