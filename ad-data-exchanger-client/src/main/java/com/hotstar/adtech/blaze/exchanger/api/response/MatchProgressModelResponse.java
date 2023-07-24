package com.hotstar.adtech.blaze.exchanger.api.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MatchProgressModelResponse {

  List<Double> deliveryProgresses;
}
