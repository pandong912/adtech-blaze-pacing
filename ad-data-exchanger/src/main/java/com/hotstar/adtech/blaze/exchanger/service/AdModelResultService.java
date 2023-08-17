package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.admodel.repository.AdModelResultDetailRepository;
import com.hotstar.adtech.blaze.admodel.repository.AdModelResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AdModelResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AdModelResultDetail;
import com.hotstar.adtech.blaze.exchanger.api.entity.AdModelDetail;
import com.hotstar.adtech.blaze.exchanger.api.response.AdModelResultUriResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdModelResultService {
  private final AdModelResultRepository adModelResultRepository;

  private final AdModelResultDetailRepository adModelResultDetailRepository;

  public Optional<AdModelResultUriResponse> queryAdModelUriByVersionGreaterThan(long version) {
    return adModelResultRepository
      .findFirstByVersionGreaterThanOrderByVersionDesc(Instant.ofEpochMilli(version))
      .map(this::buildAdModelResultResponse);
  }


  public Optional<AdModelResultUriResponse> queryAdModelUriByVersion(long version) {
    return adModelResultRepository
      .findFirstByVersionGreaterThanOrderByVersionDesc(Instant.ofEpochMilli(version))
      .map(this::buildAdModelResultResponse);
  }

  private AdModelResultUriResponse buildAdModelResultResponse(AdModelResult result) {
    List<AdModelResultDetail> adModelResultDetails =
      adModelResultDetailRepository.findAllByAdModelResultId(result.getId());
    return AdModelResultUriResponse.builder()
      .id(result.getId())
      .path(result.getPath())
      .version(result.getVersion().toEpochMilli())
      .adModelDetails(adModelResultDetails
        .stream()
        .map(this::buildAdModelResultDetailResponse)
        .collect(Collectors.toList()))
      .build();
  }

  private AdModelDetail buildAdModelResultDetailResponse(AdModelResultDetail detail) {
    return AdModelDetail.builder()
      .fileName(detail.getFileName())
      .md5(detail.getMd5())
      .build();
  }
}
