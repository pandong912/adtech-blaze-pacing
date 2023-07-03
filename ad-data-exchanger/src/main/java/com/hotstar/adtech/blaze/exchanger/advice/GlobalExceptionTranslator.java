package com.hotstar.adtech.blaze.exchanger.advice;

import com.hotstar.adtech.blaze.admodel.common.domain.ResultCode;
import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.admodel.common.exception.BusinessException;
import com.hotstar.adtech.blaze.admodel.common.exception.ParameterInvalidException;
import com.hotstar.adtech.blaze.admodel.common.exception.ResourceAlreadyExistException;
import com.hotstar.adtech.blaze.admodel.common.exception.ResourceNotFoundException;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionTranslator {

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public StandardResponse handleError(MissingServletRequestParameterException e) {
    log.warn("Missing Request Parameter", e);
    String message = String.format("Missing Request Parameter: %s", e.getParameterName());
    return StandardResponse
      .builder()
      .code(ResultCode.PARAM_MISS)
      .message(message)
      .build();
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public StandardResponse handleError(MethodArgumentTypeMismatchException e) {
    log.warn("Method Argument Type Mismatch", e);
    String message = String.format("Method Argument Type Mismatch: %s", e.getName());
    return StandardResponse
      .builder()
      .code(ResultCode.PARAM_TYPE_ERROR)
      .message(message)
      .build();
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public StandardResponse handleError(MethodArgumentNotValidException e) {
    log.warn("Method Argument Not Valid", e);
    BindingResult result = e.getBindingResult();
    FieldError error = result.getFieldError();
    String message = String.format("%s:%s", error.getField(), error.getDefaultMessage());
    return StandardResponse
      .builder()
      .code(ResultCode.PARAM_VALID_ERROR)
      .message(message)
      .build();
  }

  @ExceptionHandler(BindException.class)
  public StandardResponse handleError(BindException e) {
    log.warn("Bind Exception", e);
    FieldError error = e.getFieldError();
    String message = String.format("%s:%s", error.getField(), error.getDefaultMessage());
    return StandardResponse
      .builder()
      .code(ResultCode.PARAM_BIND_ERROR)
      .message(message)
      .build();
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public StandardResponse handleError(ConstraintViolationException e) {
    log.warn("Constraint Violation", e);
    Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
    ConstraintViolation<?> violation = violations.iterator().next();
    String path = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();
    String message = String.format("%s:%s", path, violation.getMessage());
    return StandardResponse
      .builder()
      .code(ResultCode.PARAM_VALID_ERROR)
      .message(message)
      .build();
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public StandardResponse handleError(NoHandlerFoundException e) {
    log.error("404 Not Found", e);
    return StandardResponse
      .builder()
      .code(ResultCode.NOT_FOUND)
      .message(e.getMessage())
      .build();
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public StandardResponse handleError(HttpMessageNotReadableException e) {
    log.error("Message Not Readable", e);
    return StandardResponse
      .builder()
      .code(ResultCode.MSG_NOT_READABLE)
      .message(e.getMessage())
      .build();
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public StandardResponse handleError(HttpRequestMethodNotSupportedException e) {
    log.error("Request Method Not Supported", e);
    return StandardResponse
      .builder()
      .code(ResultCode.METHOD_NOT_SUPPORTED)
      .message(e.getMessage())
      .build();
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public StandardResponse handleError(HttpMediaTypeNotSupportedException e) {
    log.error("Media Type Not Supported", e);
    return StandardResponse
      .builder()
      .code(ResultCode.MEDIA_TYPE_NOT_SUPPORTED)
      .message(e.getMessage())
      .build();
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public StandardResponse handleError(ResourceNotFoundException e) {
    log.warn("Resource Not Found", e);
    return StandardResponse
      .builder()
      .code(ResultCode.NOT_FOUND)
      .message(e.getMessage())
      .build();
  }

  @ExceptionHandler(ResourceAlreadyExistException.class)
  public StandardResponse handleError(ResourceAlreadyExistException e) {
    log.warn("Resource Already Exist", e);
    return StandardResponse
      .builder()
      .code(ResultCode.FAILURE)
      .message(e.getMessage())
      .build();
  }

  @ExceptionHandler(ParameterInvalidException.class)
  public StandardResponse handleError(ParameterInvalidException e) {
    log.warn("Parameter Invalid", e);
    return StandardResponse
      .builder()
      .code(ResultCode.FAILURE)
      .message(e.getMessage())
      .build();
  }

  @ExceptionHandler(BusinessException.class)
  public StandardResponse handleError(BusinessException e) {
    log.warn("Business Exception", e);
    return StandardResponse
      .builder()
      .code(ResultCode.FAILURE)
      .message(e.getMessage())
      .build();
  }

  @ExceptionHandler(ServiceException.class)
  public StandardResponse handleError(ServiceException e) {
    log.error("Service Exception", e);
    return StandardResponse
      .builder()
      .code(e.getResultCode())
      .message(e.getMessage())
      .build();
  }

  @ExceptionHandler(Throwable.class)
  public StandardResponse handleError(Throwable e) {
    log.error("Internal Server Error", e);
    return StandardResponse
      .builder()
      .code(ResultCode.INTERNAL_SERVER_ERROR)
      .message(e.getMessage())
      .build();
  }
}
