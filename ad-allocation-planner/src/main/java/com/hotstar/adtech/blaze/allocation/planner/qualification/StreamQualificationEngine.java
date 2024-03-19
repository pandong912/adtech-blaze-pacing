package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEvaluators;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.AspectRatioInspector;
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
public class StreamQualificationEngine {

  public BitSet qualify(PlayoutStream playoutStream, Map<Integer, AdSet> candidateAdSets, int relaxedDuration,
                        Integer breakTypeId, TargetingEvaluators evaluators) {
    BitSet qualified = (BitSet) evaluators.getActiveAdSetBitSet().clone();

    BitSet stream = evaluators.getStream().targeting(buildStreamKey(playoutStream));
    qualified.and(stream);

    BitSet streamNew = evaluators.getStreamNew().targeting(buildStreamNewKey(playoutStream));
    qualified.and(streamNew);

    BitSet breakTargeting =
      evaluators.getBreakTargeting().targeting(Collections.singleton(String.valueOf(breakTypeId)));
    qualified.and(breakTargeting);

    Predicate<Ad> adPredicate = buildAdInspector(relaxedDuration, playoutStream.getLanguage());
    BitSet ad = adTargeting(qualified, adPredicate, candidateAdSets);
    qualified.and(ad);
    return qualified;
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
        if (adSet.getSpotAds().stream().noneMatch(adPredicate)) {
          qualified.clear(adSet.getDemandId());
        }
      });
    return qualified;
  }

  private Predicate<Ad> buildAdInspector(int relaxedDuration, Language language) {
    DurationInspector durationInspector = new DurationInspector(relaxedDuration);
    LanguageInspector languageInspector = new LanguageInspector(language.getId());
    AspectRatioInspector aspectRatioInspector = new AspectRatioInspector(language.getName());
    return ad -> durationInspector.qualify(ad) && languageInspector.qualify(ad) && aspectRatioInspector.qualify(ad);
  }
}
