package com.hotstar.adtech.blaze.allocationdata.client.model;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class RequestData {
  List<Request> ssaiAndSpotRequests;
  List<Request> spotRequests;

  public RequestData(ConcurrencyData concurrencyData) {
    List<Request> ssaiAndSpotRequests = concurrencyData.getCohorts().stream()
      .map(this::buildRequest)
      .collect(Collectors.toList());
    List<Request> spotRequests = concurrencyData.getStreams().stream()
      .map(this::buildRequestInsStream)
      .collect(Collectors.toList());
    List<Request> spotRequestsInCohort = concurrencyData.getStreams().stream()
      .filter(contentStream -> contentStream.getPlayoutStream().getStreamType() == StreamType.Spot)
      .map(this::buildRequestInCohort)
      .toList();
    ssaiAndSpotRequests.addAll(spotRequestsInCohort);
    this.ssaiAndSpotRequests = ssaiAndSpotRequests;
    this.spotRequests = spotRequests;
  }

  private Request buildRequest(ContentCohort contentCohort) {
    return Request.builder()
      .concurrency(contentCohort.getConcurrency())
      .concurrencyId(contentCohort.getConcurrencyId())
      .streamType(contentCohort.getPlayoutStream().getStreamType())
      .build();
  }

  private Request buildRequestInCohort(ContentStream contentStream) {
    return Request.builder()
      .concurrency(contentStream.getConcurrency())
      .concurrencyId(contentStream.getConcurrencyIdInCohort())
      .streamType(contentStream.getPlayoutStream().getStreamType())
      .build();
  }

  private Request buildRequestInsStream(ContentStream contentStream) {
    return Request.builder()
      .concurrency(contentStream.getConcurrency())
      .concurrencyId(contentStream.getConcurrencyIdInStream())
      .streamType(contentStream.getPlayoutStream().getStreamType())
      .build();
  }
}
