package com.neoncubes.iotserver;

// import java.util.ArrayList;
// import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ObjectNode;

// This can be used to create a long integer value that can be updated atomically

import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController // short hand for @ResponseBody and @Controller
@RequestMapping("/api/message")
public class IoTMessageController {

    private static final Logger logger = LoggerFactory.getLogger(IoTMessageController.class);

    // @Autowired
    // private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IoTMessageRepository messageRepository;
    // @Autowired
    // private DeviceRepository deviceRepository;

    private Pair<Boolean, String> processUserAccess(String email, Authentication auth) {
        if (auth == null) {
            if (email == null) {
                return Pair.of(false, "No email provided and not logged in");
            } else {
                return Pair.of(false, "Requiring email and not logged in");
            }
        } else {
            logger.info("User {} has authorities: {}", email, auth.getAuthorities());
            if (email != null) {
                boolean canAccess = auth.getAuthorities()
                        .contains(new SimpleGrantedAuthority(User.UserRole.ADMIN.name()));
                if (!canAccess) {
                    return Pair.of(false, "You're not an ADMIN");
                } else {
                    return Pair.of(true, email);
                }
            } else {
                return Pair.of(true, auth.getName());
            }
        }
    }

    @GetMapping("/query")
    public ResponseEntity<?> query(@RequestParam(required = false) String email,
            @RequestParam(required = false) String mqttId, @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size, Authentication auth) {
        logger.info("Getting params: email: {}, mqttId: {}, auth: {}", email, mqttId, auth);

        Pair<Boolean, String> access = processUserAccess(email, auth);
        if (access.getFirst()) {
            email = access.getSecond();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(access.getSecond());
        }

        logger.info("Updated email from authorization: {}", email);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot find the user specified");
        } else {
            if (mqttId == null) {

                if (page == null || size == null) {
                    logger.info("Trying to find the messages for {}", user);
                    List<IoTMessage> messages = messageRepository.findByEmailOrderByDateDesc(user.getEmail());
                    logger.info("Found theses messages: {}", messages.size());

                    return ResponseEntity.status(HttpStatus.OK).body(messages);

                } else {

                    // List<Device> devices = deviceRepository.findByUser(user);
                    // logger.info("Found devices: {}", devices);

                    PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
                    // Page<IoTMessage> pages = messageRepository.findByDeviceIn(devices, request);
                    Page<IoTMessage> pages = messageRepository.findByEmailOrderByDateDesc(user.getEmail(), request);

                    logger.info("Got pages: {}", pages);

                    List<IoTMessage> messages = pages.getContent();
                    logger.info("Got messages: {}", messages);

                    return ResponseEntity.status(HttpStatus.OK).body(messages);
                }

            } else {
                return ResponseEntity.status(HttpStatus.OK).body(messageRepository.findByMqttId(mqttId));
            }
        }
    }

}