package com.hotstar.adtech.blaze.exchanger.api;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import java.util.Date;
import org.springframework.http.HttpStatus;

public class DefaultErrorDecoder implements ErrorDecoder {

  private final ErrorDecoder defaultErrorDecoder = new Default();

  @Override
  public Exception decode(String s, Response response) {
    Exception exception = defaultErrorDecoder.decode(s, response);
    if (exception instanceof RetryableException) {
      return exception;
    }

    if (response.status() != HttpStatus.OK.value()) {
      return new RetryableException(response.status(),
        "blaze data-exchanger service exception",
        response.request().httpMethod(),
        new Date(),
        response.request());
    }

    return exception;
  }
}
