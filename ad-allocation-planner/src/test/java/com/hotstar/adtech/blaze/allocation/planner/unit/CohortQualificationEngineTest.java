package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.google.common.collect.Sets;
import com.hotstar.adtech.blaze.admodel.common.enums.Platform;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.QualificationTestData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.CohortQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AudienceTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AudienceTargetingRuleClause;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CohortQualificationEngineTest {

  @Test
  public void whenContainAllNeededTagsThenSuccess() {
    ContentCohort cohort =
      ContentCohort.builder()
        .streamType(StreamType.SSAI_Spot)
        .ssaiTag("SSAI:M_MUM:M_NCR")
        .playoutStream(PlayoutStream.builder()
          .language("English")
          .platforms(Collections.singletonList(
            Platform.Android
          ))
          .tenant(Tenant.India)
          .build())
        .build();
    CohortQualificationEngine cohortQualificationEngine =
      new CohortQualificationEngine(cohort, QualificationTestData.getAttributeId2TargetingTagMap(),
        QualificationTestData.getLanguages());


    AdSet adSet = AdSet.builder()
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
    List<QualifiedAdSet> qualify = cohortQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertEquals(1, qualify.size());
    Assertions.assertEquals(2, qualify.get(0).getQualifiedAds().size());
    AdSet adSet1 = AdSet.builder()
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
    List<QualifiedAdSet> qualify1 = cohortQualificationEngine.qualify(Collections.singletonList(adSet1));
    Assertions.assertEquals(0, qualify1.size());
  }

  @Test
  public void whenContainOneNotNeededTagsThenFail() {
    ContentCohort cohort =
      ContentCohort.builder()
        .streamType(StreamType.SSAI_Spot)
        .ssaiTag("SSAI:M_MUM:M_NCR")
        .playoutStream(PlayoutStream.builder()
          .language("English")
          .platforms(Collections.singletonList(
            Platform.Android
          ))
          .tenant(Tenant.India)
          .build())
        .build();

    CohortQualificationEngine cohortQualificationEngine =
      new CohortQualificationEngine(cohort, QualificationTestData.getAttributeId2TargetingTagMap(),
        QualificationTestData.getLanguages());


    AdSet adSet = AdSet.builder()
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
    List<QualifiedAdSet> qualify = cohortQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertEquals(0, qualify.size());
  }

  @Test
  public void whenCohortTagEmptyThenFail() {
    ContentCohort cohort =
      ContentCohort.builder()
        .streamType(StreamType.SSAI_Spot)
        .ssaiTag("SSAI::")
        .playoutStream(PlayoutStream.builder()
          .language("English")
          .platforms(Collections.singletonList(
            Platform.Android
          ))
          .tenant(Tenant.India)
          .build())
        .build();

    CohortQualificationEngine cohortQualificationEngine =
      new CohortQualificationEngine(cohort, QualificationTestData.getAttributeId2TargetingTagMap(),
        QualificationTestData.getLanguages());

    AdSet adSet = AdSet.builder()
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
    List<QualifiedAdSet> qualify = cohortQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertEquals(0, qualify.size());

    AdSet adSet2 = AdSet.builder()
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
    List<QualifiedAdSet> qualify2 = cohortQualificationEngine.qualify(Collections.singletonList(adSet2));
    Assertions.assertEquals(0, qualify2.size());
  }

  @Test
  public void whenContainNeededTagAndContainNotNeededTagThenFail() {
    ContentCohort cohort =
      ContentCohort.builder()
        .streamType(StreamType.SSAI_Spot)
        .ssaiTag("SSAI:M_MUM:M_NCR:S_APTG")
        .playoutStream(PlayoutStream.builder()
          .language("English")
          .platforms(Collections.singletonList(
            Platform.Android
          ))
          .tenant(Tenant.India)
          .build())
        .build();

    CohortQualificationEngine cohortQualificationEngine =
      new CohortQualificationEngine(cohort, QualificationTestData.getAttributeId2TargetingTagMap(),
        QualificationTestData.getLanguages());


    AdSet adSet = AdSet.builder()
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
    List<QualifiedAdSet> qualify = cohortQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertEquals(0, qualify.size());
  }
}
