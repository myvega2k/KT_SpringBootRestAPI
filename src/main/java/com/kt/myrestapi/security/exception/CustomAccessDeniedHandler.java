package com.kt.myrestapi.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        // Set response content type to JSON
        response.setContentType("application/json;charset=UTF-8");
        // Set response code
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ObjectMapper mapper = new ObjectMapper();
        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpServletResponse.SC_FORBIDDEN);
        errorObject.setMessage("요청된 리소스에 대한 유효한 권한이 없습니다!!");

        // Add content to the response
        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(errorObject);
        response.getWriter().write(json);

    }
}