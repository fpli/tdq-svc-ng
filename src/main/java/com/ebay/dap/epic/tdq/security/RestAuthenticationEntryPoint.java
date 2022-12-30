package com.ebay.dap.epic.tdq.security;

import com.ebay.dap.epic.tdq.common.Constants;
import com.ebay.dap.epic.tdq.web.protocal.response.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        response.setCharacterEncoding(Constants.UTF_8);
        response.setContentType(Constants.APPLICATION_JSON);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, e.getMessage(), e);

        PrintWriter printWriter = response.getWriter();
        printWriter.append(objectMapper.writeValueAsString(apiError));
    }
}
