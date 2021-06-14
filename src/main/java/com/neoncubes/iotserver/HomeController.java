package com.neoncubes.iotserver;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class HomeController {

    @Autowired
    private UserController userController;

    Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping(value = {"/signup", "/signin"})
    public String index(Authentication authentication) {
        logger.info("Loading page from index.html");
        logger.info("Getting auth: {}", authentication);
        if (authentication != null) {
            logger.info("User is already logged in!");
        } else {
            logger.info("This user is currently anonymous");
        }
        return "index.html";
    }
}
