package com.ebay.dap.epic.tdq.web.protocal.response;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ApiError {
  private int status;
  private String message;
  private String path;
  private ZonedDateTime timestamp;
  private String error;
  private List<ApiSubError> errors;

  public ApiError() {
    timestamp = ZonedDateTime.now();
  }

  public ApiError(HttpStatus status) {
    this();
    this.status = status.value();
  }

  public ApiError(HttpStatus status, Throwable ex) {
    this();
    this.status = status.value();
    this.error = "Unexpected error";
    this.message = ex.getLocalizedMessage();
  }

  public ApiError(HttpStatus status, String message, Throwable ex) {
    this();
    this.status = status.value();
    this.error = message;
    this.message = ex.getLocalizedMessage();
  }

  private void addSubError(ApiSubError subError) {
    if (errors == null) {
      errors = new ArrayList<>();
    }
    errors.add(subError);
  }

}
