package com.neoncubes.iotserver;

import java.util.ArrayList;

// This can be used to create a long integer value that can be updated atomically
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController // short hand for @ResponseBody and @Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository repo;

    // @PostMapping("/user/register")
    @GetMapping("/")
    public String helloAdmin() {
        return "hello admin";
    }

}
