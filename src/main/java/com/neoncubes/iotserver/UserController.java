package com.neoncubes.iotserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;

@RestController // short hand for @ResponseBody and @Controller
@RequestMapping("/api")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper mapper;

    @GetMapping("/account/auth")
    public ResponseEntity<?> auth(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You're not logged in.");
        } else {
            ObjectNode authNode = mapper.createObjectNode();
            authNode.put("email", auth.getName());
            return ResponseEntity.status(HttpStatus.OK).body(authNode);
        }
    }

    @PostMapping({ "/account/checkemail", "/user/checkemail" })
    public ResponseEntity<?> checkemail(@RequestBody JsonNode node) {
        logger.info("Finding by json node: {}", node);
        String email = node.get("email").asText();
        User user = repo.findByEmail(email);
        if (user != null) {
            return ResponseEntity.status(HttpStatus.OK).body("This email exists.");
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

    @PostMapping({ "/account/create", "/user/create" })
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
