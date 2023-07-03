package com.hotstar.adtech.blaze.exchanger.api.response;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class TournamentInfoResponse {
  Long seasonId;
  Long tournamentId;
  String contentId;
  String siMatchId;
}
