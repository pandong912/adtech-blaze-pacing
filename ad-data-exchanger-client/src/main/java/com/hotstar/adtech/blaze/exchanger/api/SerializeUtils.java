package com.hotstar.adtech.blaze.exchanger.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public class SerializeUtils {

  public static ObjectMapper objectMapper() {
    return new ObjectMapper()
      .findAndRegisterModules()
      .registerModule(new ParameterNamesModule(JsonCreator.Mode.DEFAULT))
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

}
