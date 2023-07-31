package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.QualificationTestData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Platform;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.StreamTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.StreamTargetingRuleClause;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StreamQualificationEngineTest {

  @Test
  public void whenIncludeRuleIsAllMatchThenSuccess() {
    ContentStream stream =
      ContentStream.builder()
        .playoutStream(PlayoutStream.builder()
          .streamType(StreamType.SSAI_Spot)
          .language(Language.builder().name("English").build())
          .platforms(Arrays.asList(
              Platform.builder().name("Android").build(),
              Platform.builder().name("iOS").build())
          )
          .tenant(Tenant.India)
          .build())
        .build();
    StreamQualificationEngine streamQualificationEngine =
      new StreamQualificationEngine(stream.getPlayoutStream());


    AdSet adSet = AdSet.builder()
      .id(1)
      .spotAds(QualificationTestData.getAds())
      .streamTargetingRule(StreamTargetingRule.builder()
        .tenant(Tenant.India)
        .ruleType(RuleType.Include)
        .streamTargetingRuleClauses(Arrays.asList(
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(1)
            .languageId(1)
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(2)
            .languageId(1)
            .build()
        ))
        .build())
      .build();
    List<QualifiedAdSet> qualify = streamQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertEquals(1, qualify.size());
    Assertions.assertEquals(2, qualify.get(0).getQualifiedAds().size());

    AdSet adSet2 = AdSet.builder()
      .id(1)
      .spotAds(QualificationTestData.getAds())
      .streamTargetingRule(StreamTargetingRule.builder()
        .tenant(Tenant.India)
        .ruleType(RuleType.Include)
        .streamTargetingRuleClauses(Arrays.asList(
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(1)
            .languageId(1)
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(8)
            .languageId(1)
            .build()
        ))
        .build())
      .build();
    List<QualifiedAdSet> qualify2 = streamQualificationEngine.qualify(Collections.singletonList(adSet2));
    Assertions.assertEquals(0, qualify2.size());
  }

  @Test
  public void whenExcludeRuleNoneMatchThenSuccess() {
    ContentStream stream =
      ContentStream.builder()
        .playoutStream(PlayoutStream.builder()
          .streamType(StreamType.SSAI_Spot)
          .language(Language.builder().name("English").build())
          .platforms(Arrays.asList(
            Platform.builder().name("Android").build(),
            Platform.builder().name("iOS").build()
          ))
          .tenant(Tenant.India)
          .build())
        .build();
    StreamQualificationEngine streamQualificationEngine =
      new StreamQualificationEngine(stream.getPlayoutStream());


    AdSet adSet = AdSet.builder()
      .id(1)
      .spotAds(QualificationTestData.getAds())
      .streamTargetingRule(StreamTargetingRule.builder()
        .tenant(Tenant.India)
        .ruleType(RuleType.Exclude)
        .streamTargetingRuleClauses(Arrays.asList(
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(6)
            .languageId(1)
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(8)
            .languageId(1)
            .build()
        ))
        .build())
      .build();
    List<QualifiedAdSet> qualify = streamQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertEquals(1, qualify.size());
    Assertions.assertEquals(2, qualify.get(0).getQualifiedAds().size());

    AdSet adSet2 = AdSet.builder()
      .id(1)
      .spotAds(QualificationTestData.getAds())
      .streamTargetingRule(StreamTargetingRule.builder()
        .tenant(Tenant.India)
        .ruleType(RuleType.Exclude)
        .streamTargetingRuleClauses(Arrays.asList(
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(6)
            .languageId(2)
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(8)
            .languageId(1)
            .languageId(1)
            .build()
        ))
        .build())
      .build();
    List<QualifiedAdSet> qualify2 = streamQualificationEngine.qualify(Collections.singletonList(adSet2));
    Assertions.assertEquals(1, qualify2.size());
  }

}
