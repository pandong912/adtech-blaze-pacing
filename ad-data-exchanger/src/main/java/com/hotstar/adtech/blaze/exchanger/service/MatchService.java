package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.admodel.repository.MatchRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.Match;
import com.hotstar.adtech.blaze.exchanger.api.response.TournamentInfoResponse;
import com.hotstar.adtech.blaze.exchanger.config.CacheConfig;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchService {

  private final MatchRepository matchRepository;

  @Cacheable(cacheNames = CacheConfig.SEASON_ID_BY_CONTENT,
    cacheManager = CacheConfig.DATABASE_CACHE_MANAGER,
    sync = true)
  public Optional<TournamentInfoResponse> getSeasonIdByContentId(String contentId) {
    return matchRepository.findByContentId(contentId)
      .map(this::buildTournamentInfoResponse);
  }

  private TournamentInfoResponse buildTournamentInfoResponse(Match match) {
    return TournamentInfoResponse.builder()
      .seasonId(match.getSeasonId())
      .tournamentId(match.getTournamentId())
      .contentId(match.getContentId())
      .siMatchId(match.getSiMatchId())
      .build();
  }
}
