package com.hotstar.adtech.blaze.exchanger.advice;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class RequestParamTrimAdvice {

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor("\t\r\n", false);
    binder.registerCustomEditor(String.class, stringTrimmerEditor);
  }
}
