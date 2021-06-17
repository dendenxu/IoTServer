package com.neoncubes.iotserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EmailPasswordFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response)
            throws AuthenticationException {

        // ...

        logger.info("Got authorization request: {}", request);

        try {
            UsernamePasswordAuthenticationToken authRequest = getAuthRequest(request);
            setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new AuthenticationServiceException("IO Error when parsing payload");
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(EmailPasswordFilter.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private UsernamePasswordAuthenticationToken getAuthRequest(
            HttpServletRequest request) throws IOException {

        AuthBody authBody = mapper.readValue(request.getReader(), AuthBody.class);

        logger.info("Got authorization attempt: {}", authBody);

        String username = authBody.getEmail();
        String password = authBody.getPasswd();

        return new UsernamePasswordAuthenticationToken(
                username, password);
    }

    // other methods

    @Data
    public static class AuthBody {
        private String email;
        private String passwd;
    }

}