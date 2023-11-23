package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.google.common.collect.Sets;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.QualificationTestData;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AudienceTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AudienceTargetingRuleClause;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Platform;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.CohortAdSetQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BitSetQualificationResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationResult;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CohortQualificationEngineTest {
  private final Map<String, Language> languageMapping = QualificationTestData.getLanguageMapping();
  private final Map<String, Platform> platformMapping = QualificationTestData.getPlatformMapping();

  @Test
  public void whenContainAllNeededTagsThenSuccess() {
    ContentCohort cohort =
      ContentCohort.builder()
        .concurrencyId(0)
        .ssaiTag("SSAI:M_MUM:M_NCR")
        .playoutStream(PlayoutStream.builder()
          .streamType(StreamType.SSAI_Spot)
          .language(languageMapping.get("English"))
          .platforms(Collections.singletonList(platformMapping.get("Android")))
          .tenant(Tenant.India)
          .build())
        .build();
    QualificationResult bitSet = new BitSetQualificationResult(10, 2);
    CohortAdSetQualificationEngine cohortQualificationEngine =
      new CohortAdSetQualificationEngine(cohort.getSsaiTag(), QualificationTestData.getAttributeId2TargetingTagMap(),
        cohort.getConcurrencyId(), bitSet);


    AdSet adSet = AdSet.builder()
      .demandId(0)
      .ssaiAds(QualificationTestData.getAds())
      .audienceTargetingRule(AudienceTargetingRule.builder()
        .includes(Collections.singletonList(AudienceTargetingRuleClause.builder()
          .categoryId(2)
          .targetingTags(Sets.newHashSet("M_MUM", "M_NCR", "M_BEN"))
          .build()
        ))
        .excludes(Collections.emptyList())
        .build())
      .build();
    cohortQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertTrue(bitSet.get(0, 0));
    AdSet adSet1 = AdSet.builder()
      .demandId(1)
      .ssaiAds(QualificationTestData.getAds())
      .audienceTargetingRule(AudienceTargetingRule.builder()
        .includes(Collections.singletonList(AudienceTargetingRuleClause.builder()
          .categoryId(2)
          .targetingTags(Sets.newHashSet("M_MUM"))
          .build()
        ))
        .excludes(Collections.emptyList())
        .build())
      .build();
    cohortQualificationEngine.qualify(Collections.singletonList(adSet1));
    Assertions.assertFalse(bitSet.get(0, 1));
  }

  @Test
  public void whenContainOneNotNeededTagsThenFail() {
    ContentCohort cohort =
      ContentCohort.builder()
        .concurrencyId(0)
        .ssaiTag("SSAI:M_MUM:M_NCR")
        .playoutStream(PlayoutStream.builder()
          .streamType(StreamType.SSAI_Spot)
          .language(languageMapping.get("English"))
          .platforms(Collections.singletonList(platformMapping.get("Android")))
          .tenant(Tenant.India)
          .build())
        .build();

    QualificationResult bitSet = new BitSetQualificationResult(10, 2);
    CohortAdSetQualificationEngine cohortQualificationEngine =
      new CohortAdSetQualificationEngine(cohort.getSsaiTag(), QualificationTestData.getAttributeId2TargetingTagMap(),
        cohort.getConcurrencyId(), bitSet);


    AdSet adSet = AdSet.builder()
      .demandId(0)
      .ssaiAds(QualificationTestData.getAds())
      .audienceTargetingRule(AudienceTargetingRule.builder()
        .includes(Collections.emptyList())
        .excludes(Collections.singletonList(AudienceTargetingRuleClause.builder()
          .categoryId(2)
          .targetingTags(Sets.newHashSet("M_NCR", "M_BEN"))
          .build()
        ))
        .build())
      .build();
    cohortQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertFalse(bitSet.get(0, 0));
  }

  @Test
  public void whenCohortTagEmptyThenFail() {
    ContentCohort cohort =
      ContentCohort.builder()
        .concurrencyId(0)
        .ssaiTag("SSAI::")
        .playoutStream(PlayoutStream.builder()
          .streamType(StreamType.SSAI_Spot)
          .language(languageMapping.get("English"))
          .platforms(Collections.singletonList(platformMapping.get("Android")))
          .tenant(Tenant.India)
          .build())
        .build();

    QualificationResult bitSet = new BitSetQualificationResult(10, 2);
    CohortAdSetQualificationEngine cohortQualificationEngine =
      new CohortAdSetQualificationEngine(cohort.getSsaiTag(), QualificationTestData.getAttributeId2TargetingTagMap(),
        cohort.getConcurrencyId(), bitSet);

    AdSet adSet = AdSet.builder()
      .demandId(0)
      .ssaiAds(QualificationTestData.getAds())
      .audienceTargetingRule(AudienceTargetingRule.builder()
        .includes(Collections.emptyList())
        .excludes(Collections.singletonList(AudienceTargetingRuleClause.builder()
          .categoryId(2)
          .targetingTags(Sets.newHashSet("M_NCR", "M_BEN"))
          .build()
        ))
        .build())
      .build();
    cohortQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertFalse(bitSet.get(0, 0));

    AdSet adSet2 = AdSet.builder()
      .demandId(1)
      .ssaiAds(QualificationTestData.getAds())
      .audienceTargetingRule(AudienceTargetingRule.builder()
        .includes(Collections.singletonList(AudienceTargetingRuleClause.builder()
          .categoryId(2)
          .targetingTags(Sets.newHashSet("M_MUM", "M_NCR", "M_BEN"))
          .build()
        ))
        .excludes(Collections.emptyList())
        .build())
      .build();
    cohortQualificationEngine.qualify(Collections.singletonList(adSet2));
    Assertions.assertFalse(bitSet.get(0, 1));
  }

  @Test
  public void whenContainNeededTagAndContainNotNeededTagThenFail() {
    ContentCohort cohort =
      ContentCohort.builder()
        .concurrencyId(0)
        .ssaiTag("SSAI:M_MUM:M_NCR:S_APTG")
        .playoutStream(PlayoutStream.builder()
          .streamType(StreamType.SSAI_Spot)
          .language(languageMapping.get("English"))
          .platforms(Collections.singletonList(platformMapping.get("Android")))
          .tenant(Tenant.India)
          .build())
        .build();

    QualificationResult bitSet = new BitSetQualificationResult(10, 2);
    CohortAdSetQualificationEngine cohortQualificationEngine =
      new CohortAdSetQualificationEngine(cohort.getSsaiTag(), QualificationTestData.getAttributeId2TargetingTagMap(),
        cohort.getConcurrencyId(), bitSet);


    AdSet adSet = AdSet.builder()
      .demandId(0)
      .ssaiAds(QualificationTestData.getAds())
      .audienceTargetingRule(AudienceTargetingRule.builder()
        .includes(Collections.singletonList(AudienceTargetingRuleClause.builder()
          .categoryId(2)
          .targetingTags(Sets.newHashSet("M_MUM", "M_NCR", "M_BEN"))
          .build()
        ))
        .excludes(Collections.singletonList(AudienceTargetingRuleClause.builder()
          .categoryId(1)
          .targetingTags(Sets.newHashSet("S_APTG"))
          .build()
        ))
        .build())
      .build();
    cohortQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertFalse(bitSet.get(0, 0));
  }
}
