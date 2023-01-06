package com.ebay.dap.epic.tdq.web.protocal.response;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ApiError extends RestApiResponse {
    private int status;
    private String message;
    private String path;
    private ZonedDateTime timestamp;
    private List<ErrorItem> errors = new ArrayList<>();

    private ApiError() {
        timestamp = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public ApiError(HttpStatus status) {
        this();
        this.status = status.value();
        this.message = status.getReasonPhrase();
    }

    public ApiError(HttpStatus status, Throwable ex) {
        this();
        this.status = status.value();
        this.message = ex.getLocalizedMessage();
        this.getErrors().add(new ErrorItem(10000, ex.getLocalizedMessage()));
    }

    public ApiError(HttpStatus status, String message, Throwable ex) {
        this();
        this.status = status.value();
        this.message = message;
        this.getErrors().add(new ErrorItem(10000, ex.getLocalizedMessage()));
    }

    private void addErrorItem(ErrorItem item) {
        errors.add(item);
    }

}
