package com.hotstar.adtech.blaze.exchanger.api.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Md5Value {
  Long breakTypeId;
  String md5;
}
