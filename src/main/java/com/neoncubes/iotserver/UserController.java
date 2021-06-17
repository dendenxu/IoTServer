package com.neoncubes.iotserver;

import java.util.ArrayList;

// This can be used to create a long integer value that can be updated atomically
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping({"/account/checkemail", "/user/checkemail"})
    public ResponseEntity<?> checkemail(
            @RequestBody JsonNode node
    ) {
        logger.info("Finding by email: {}", node);
        String email = node.get("email").asText();
        User user = repo.findByEmail(email);
        if (user != null) {
            return ResponseEntity.status(HttpStatus.OK).body("This email exists");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cannot find the user specified.");
        }
    }

    @GetMapping("/user/query")
    public ResponseEntity<?> user(@RequestParam(value = "email", defaultValue = "") String email) {
        logger.info("Finding by email: {}", email);
        User user = repo.findByEmail(email);
        logger.info("Found: {}", user);
        if (user != null) {
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cannot find the user specified.");
        }
    }

    @PostMapping({"/account/create", "/user/create"})
    public ResponseEntity<?> register(@RequestBody User user) {
        logger.info("The server received this: {}", user);
        if (repo.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The email already exists.");
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            repo.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("OK, the server has remembered you.");
        }
    }

    @PatchMapping("/user/replace")
    public ResponseEntity<?> replace(@RequestBody User user) {
        logger.info("The server received this: {}", user);
        if (repo.findByEmail(user.getEmail()) == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The email doesn't exist.");
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            repo.save(user);
            return ResponseEntity.status(HttpStatus.OK).body("OK, the server has remembered the new you.");
        }
    }

    @DeleteMapping("/user/delete")
    public ResponseEntity<?> delete(@RequestBody User user) {
        logger.info("The server received this: {}", user);
        if (repo.findByEmail(user.getEmail()) == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The email doesn't exist.");
        } else {
            repo.delete(user);
            return ResponseEntity.status(HttpStatus.OK).body("OK, the server has deleted you.");
        }
    }
}
