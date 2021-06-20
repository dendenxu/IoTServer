package com.neoncubes.iotserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

    @Data
    @AllArgsConstructor
    public static class ErrorData {
        String error;
        Object data;
    }

    @Autowired
    private ObjectMapper mapper;

    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authenticationException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorData data = new ErrorData(authenticationException.getMessage(), null);
        response.getWriter().write(mapper.writeValueAsString(data)); // Write response body.

    }
}