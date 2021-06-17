package com.neoncubes.iotserver;

import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class HomeController {

    @Autowired
    private UserController userController;

    Logger logger = LoggerFactory.getLogger(HomeController.class);

    // FIXME: This is deprecated if using external frontend
//    @GetMapping(value = {"/signup", "/signin"})
//    public String index(Authentication authentication) {
//        logger.info("Loading page from index.html");
//        logger.info("Getting auth: {}", authentication);
//        if (authentication != null) {
//            logger.info("User is already logged in!");
//        } else {
//            logger.info("This user is currently anonymous");
//        }
//        return "index.html";
//    }

//    @RequestMapping("/api/account")
//    public void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
//
//        logger.info("Getting request to be redirected: {}", request);
//        response.sendRedirect("/api/user");
//    }
}
