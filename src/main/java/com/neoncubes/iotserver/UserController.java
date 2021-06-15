package com.neoncubes.iotserver;

import java.util.ArrayList;

// This can be used to create a long integer value that can be updated atomically
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import org.springframework.http.ResponseEntity;

@RestController // short hand for @ResponseBody and @Controller
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // @PostMapping("/user/signin")
    // private ResponseEntity<?> login(@RequestBody User user) {
    // User found = repo.findByEmail(user.getEmail());
    // logger.info("Provided user: {}", user);
    // logger.info("Found matching: {}", found);
    // if (found != null && found.getPassword().equals(user.getPassword())) {
    // user.setPassword("password hash redacted");
    // return ResponseEntity.status(200).body(found);
    // } else {
    // return ResponseEntity.status(404).body("Cannot find the user specified or the
    // password is incorrect.");
    // }
    // }

    @GetMapping("/query")
    public ResponseEntity<?> user(@RequestParam(value = "email", defaultValue = "") String email) {
        logger.info("Finding by email: {}", email);
        User user = repo.findByEmail(email);
        logger.info("Found: {}", user);
        if (user != null) {
            // user.setPassword("password hash redacted");
            return ResponseEntity.status(200).body(user);
        } else {
            return ResponseEntity.status(404).body("Cannot find the user specified");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        logger.info("The server received this: {}", user);
        if (repo.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.status(409).body("The email already exists.");
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            repo.save(user);
            return ResponseEntity.status(200).body("OK, the server has remembered you.");
        }
    }

    @PatchMapping("/replace")
    public ResponseEntity<?> replace(@RequestBody User user) {
        logger.info("The server received this: {}", user);
        if (repo.findByEmail(user.getEmail()) == null) {
            return ResponseEntity.status(409).body("The email doesn't exist.");
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            repo.save(user);
            return ResponseEntity.status(200).body("OK, the server has remembered the new you.");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody User user) {
        logger.info("The server received this: {}", user);
        if (repo.findByEmail(user.getEmail()) == null) {
            return ResponseEntity.status(409).body("The email doesn't exist.");
        } else {
            repo.delete(user);
            return ResponseEntity.status(200).body("OK, the server has deleted you.");
        }
    }
}
