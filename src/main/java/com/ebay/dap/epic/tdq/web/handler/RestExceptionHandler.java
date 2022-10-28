package com.ebay.dap.epic.tdq.web.handler;

import com.ebay.dap.epic.tdq.common.exception.ApiException;
import com.ebay.dap.epic.tdq.common.exception.ResourceNotFoundException;
import com.ebay.dap.epic.tdq.web.protocal.response.ApiError;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Object> handle(ApiException ex, WebRequest request) {
    log.error("Error:", ex);
    ApiError apiError = new ApiError();

    apiError.setPath(getPath(request));
    apiError.setMessage(ex.getMessage());

    if (ex instanceof ResourceNotFoundException) {
      apiError.setStatus(HttpStatus.NOT_FOUND.value());
      apiError.setError("Resource not found");
    } else {
      apiError.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
      apiError.setError("Unexpected server error");
    }

    return buildResponseEntity(apiError);
  }

  private String getPath(WebRequest request) {
    HttpServletRequest httpRequest = ((ServletWebRequest) request).getRequest();
    StringBuilder sb = new StringBuilder(httpRequest.getServletPath());
    if (StringUtils.isNotBlank(httpRequest.getQueryString())) {
      sb.append("?").append(httpRequest.getQueryString());
    }
    return sb.toString();
  }

  private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
    return new ResponseEntity<>(apiError, HttpStatus.valueOf(apiError.getStatus()));
  }
}
