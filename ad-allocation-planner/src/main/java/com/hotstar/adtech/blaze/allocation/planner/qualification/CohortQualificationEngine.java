package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.util.BitSetUtil;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEvaluators;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.DurationInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.LanguageInspector;
import java.util.BitSet;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CohortQualificationEngine {


  public BitSet qualify(PlayoutStream playoutStream, Map<Integer, AdSet> candidateAdSets, int relaxedDuration,
                        Map<Integer, Set<String>> categoryId2Tags, TargetingEvaluators evaluators) {

    BitSet qualified = (BitSet) evaluators.getActiveAdSetBitSet().clone();

    BitSet stream = evaluators.getStream().targeting(buildStreamKey(playoutStream));
    qualified.and(stream);

    BitSet streamNew = evaluators.getStreamNew().targeting(buildStreamNewKey(playoutStream));
    qualified.and(streamNew);

    BitSet audience = evaluators.getAudience().entrySet().stream()
      .map(e -> categoryTargeting(categoryId2Tags, e))
      .reduce(BitSetUtil.allTrue(evaluators.getAdSetSize()), BitSetUtil::and);
    qualified.and(audience);

    Predicate<Ad> adPredicate = buildAdInspector(relaxedDuration, playoutStream.getLanguage());
    BitSet ad = adTargeting(qualified, adPredicate, candidateAdSets);
    qualified.and(ad);
    return qualified;
  }

  private static BitSet categoryTargeting(Map<Integer, Set<String>> categoryId2Tags,
                                          Map.Entry<Integer, TargetingEngine> e) {
    TargetingEngine categoryTargetingEngine = e.getValue();
    Set<String> tags = categoryId2Tags.getOrDefault(e.getKey(), Collections.emptySet());
    return categoryTargetingEngine.targeting(tags);
  }

  private Set<String> buildStreamNewKey(PlayoutStream playoutStream) {
    Language language = playoutStream.getLanguage();
    StreamType streamType = playoutStream.getStreamType();
    return playoutStream.getLadders()
      .stream()
      .map(l -> playoutStream.getTenant() + "+" + language.getId() + "+" + l + "+" + streamType)
      .collect(Collectors.toSet());
  }

  private Set<String> buildStreamKey(PlayoutStream playoutStream) {
    return playoutStream.getPlatforms()
      .stream()
      .map(platform -> playoutStream.getTenant() + "+" + playoutStream.getLanguage().getId() + "+" + platform.getId())
      .collect(Collectors.toSet());
  }

  private BitSet adTargeting(BitSet preQualified, Predicate<Ad> adPredicate, Map<Integer, AdSet> candidateAdSets) {
    BitSet qualified = (BitSet) preQualified.clone();
    preQualified.stream()
      .mapToObj(candidateAdSets::get)
      .forEach(adSet -> {
        if (adSet.getSsaiAds().stream().noneMatch(adPredicate)) {
          qualified.clear(adSet.getDemandId());
        }
      });
    return qualified;
  }

  private Predicate<Ad> buildAdInspector(int relaxedDuration, Language language) {
    DurationInspector durationInspector = new DurationInspector(relaxedDuration);
    LanguageInspector languageInspector = new LanguageInspector(language.getId());
    return ad -> durationInspector.qualify(ad) && languageInspector.qualify(ad);
  }
}
