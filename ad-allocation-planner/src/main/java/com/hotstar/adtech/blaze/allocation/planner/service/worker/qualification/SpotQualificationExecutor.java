package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.QUALIFICATION;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamAdQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamAdSetQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import com.hotstar.adtech.blaze.allocation.planner.util.MemoryAlignment;
import io.micrometer.core.annotation.Timed;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SpotQualificationExecutor {


  @Timed(value = QUALIFICATION, extraTags = {"type", "spot"})
  public List<GraphContext> executeQualify(GeneralPlanContext generalPlanContext,
                                           List<BreakTypeGroup> breakTypeGroups) {

    List<AdSet> adSets = generalPlanContext.getAdSets();
    List<ContentStream> streams = generalPlanContext.getConcurrencyData().getStreams();

    BitSet firstQualified = new BitSet(MemoryAlignment.getSize(adSets) * streams.size());

    streams.forEach(contentStream -> qualifyByConcurrency(contentStream, adSets, firstQualified));

    return breakTypeGroups.parallelStream()
      .flatMap(breakTypeGroup -> breakTypeGroup.getAllBreakDurations()
        .parallelStream()
        .map(breakDuration -> buildGraphContextForEachPlan(generalPlanContext, breakTypeGroup, firstQualified,
          breakDuration)))
      .collect(Collectors.toList());

  }

  private GraphContext buildGraphContextForEachPlan(GeneralPlanContext generalPlanContext,
                                                    BreakTypeGroup breakTypeGroup, BitSet firstQualified,
                                                    Integer breakDuration) {
    BitSet secondQualified = new BitSet(firstQualified.size());
    generalPlanContext.getConcurrencyData().getStreams()
      .forEach(
        request -> qualifyByBreak(generalPlanContext.getAdSets(), request, breakTypeGroup.getBreakTypeIds().get(0),
          breakDuration, firstQualified,
          secondQualified));

    return GraphContext.builder()
      .breakDuration(breakDuration)
      .breakTypeGroup(breakTypeGroup)
      .planType(PlanType.SPOT)
      .requests(generalPlanContext.getSpotRequests())
      .edges(secondQualified)
      .responses(generalPlanContext.getResponses())
      .build();
  }


  private void qualifyByConcurrency(ContentStream contentStream, List<AdSet> adSets, BitSet firstQualified) {
    QualificationEngine qualificationEngine =
      new StreamAdSetQualificationEngine(contentStream.getPlayoutStream(), contentStream.getConcurrencyId(),
        firstQualified);
    qualificationEngine.qualify(adSets);
  }

  private void qualifyByBreak(List<AdSet> adSets, ContentStream request, Integer breakTypeId, Integer breakDuration,
                              BitSet firstQualified, BitSet secondQualified) {
    Language language = request.getPlayoutStream().getLanguage();
    QualificationEngine qualificationEngine =
      new StreamAdQualificationEngine(breakDuration, breakTypeId, language, request.getConcurrencyId(),
        firstQualified, secondQualified);

    qualificationEngine.qualify(adSets);
  }
}
