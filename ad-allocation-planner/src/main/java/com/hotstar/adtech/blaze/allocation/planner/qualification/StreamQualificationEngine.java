package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamView;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEvaluators;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.AspectRatioInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.DurationInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.LanguageInspector;
import java.util.BitSet;
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
    BitSet ad = adTargeting(qualified, adPredicate, candidateAdSets);
    qualified.and(ad);
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
