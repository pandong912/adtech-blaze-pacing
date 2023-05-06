package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.hotstar.adtech.blaze.admodel.client.model.LanguageInfo;
import com.hotstar.adtech.blaze.admodel.common.enums.Platform;
import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.QualificationTestData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
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
        .streamType(StreamType.SSAI_Spot)
        .playoutStream(PlayoutStream.builder()
          .language("English")
          .platforms(Arrays.asList(
            Platform.Android,
            Platform.iOS
          ))
          .tenant(Tenant.India)
          .build())
        .build();
    StreamQualificationEngine streamQualificationEngine =
      new StreamQualificationEngine(stream, QualificationTestData.getLanguages());


    AdSet adSet = AdSet.builder()
      .id(1)
      .spotAds(QualificationTestData.getAds())
      .streamTargetingRule(StreamTargetingRule.builder()
        .tenant(Tenant.India)
        .ruleType(RuleType.Include)
        .streamTargetingRuleClauses(Arrays.asList(
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platform(Platform.Android)
            .language(LanguageInfo.builder().id(1).name("English").build())
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platform(Platform.iOS)
            .language(LanguageInfo.builder().id(1).name("English").build())
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
            .platform(Platform.Android)
            .language(LanguageInfo.builder().id(1).name("English").build())
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platform(Platform.FireTV)
            .language(LanguageInfo.builder().id(1).name("English").build())
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
        .streamType(StreamType.SSAI_Spot)
        .playoutStream(PlayoutStream.builder()
          .language("English")
          .platforms(Arrays.asList(
            Platform.Android,
            Platform.iOS
          ))
          .tenant(Tenant.India)
          .build())
        .build();
    StreamQualificationEngine streamQualificationEngine =
      new StreamQualificationEngine(stream, QualificationTestData.getLanguages());


    AdSet adSet = AdSet.builder()
      .id(1)
      .spotAds(QualificationTestData.getAds())
      .streamTargetingRule(StreamTargetingRule.builder()
        .tenant(Tenant.India)
        .ruleType(RuleType.Exclude)
        .streamTargetingRuleClauses(Arrays.asList(
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platform(Platform.AndroidTV)
            .language(LanguageInfo.builder().id(1).name("English").build())
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platform(Platform.FireTV)
            .language(LanguageInfo.builder().id(1).name("English").build())
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
            .platform(Platform.AndroidTV)
            .language(LanguageInfo.builder().id(1).name("Hindi").build())
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platform(Platform.FireTV)
            .language(LanguageInfo.builder().id(1).name("English").build())
            .build()
        ))
        .build())
      .build();
    List<QualifiedAdSet> qualify2 = streamQualificationEngine.qualify(Collections.singletonList(adSet2));
    Assertions.assertEquals(1, qualify2.size());
  }

}
