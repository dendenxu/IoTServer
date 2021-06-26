package com.neoncubes.iotserver;

import java.text.SimpleDateFormat;
import java.util.Date;
// import java.util.ArrayList;
// import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    // @Autowired
    // private UserRepository userRepository;
    @Autowired
    private IoTMessageRepository messageRepository;
    @Autowired
    private DeviceRepository deviceRepository;

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

        if (mqttId == null) {

            if (page == null || size == null) {
                Stream<IoTMessage> messages = messageRepository.findStreamByEmailOrderByDateDesc(email, null);
                logger.info("Found theses messages: {}", messages.count());

                return ResponseEntity.status(HttpStatus.OK).body(messages);

            } else {

                // List<Device> devices = deviceRepository.findByUser(user);
                // logger.info("Found devices: {}", devices);

                PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
                // Page<IoTMessage> pages = messageRepository.findByDeviceIn(devices, request);
                Page<IoTMessage> pages = messageRepository.findPageByEmailOrderByDateDesc(email, request);

                logger.info("Got pages: {}", pages);

                List<IoTMessage> messages = pages.getContent();
                logger.info("Got messages: {}", messages);

                return ResponseEntity.status(HttpStatus.OK).body(messages);
            }

        } else {
            return ResponseEntity.status(HttpStatus.OK).body(messageRepository.findByMqttId(mqttId));
        }
    }

    @Autowired
    private ObjectMapper mapper;

    // http://localhost:8080/api/message/count?mqttId=device0000&fromMills=1624666885920&toMills=1624684885920
    @GetMapping("/count")
    public ResponseEntity<?> count(@RequestParam(required = false) String email,
            @RequestParam(required = false) String mqttId, @RequestParam(required = false) Date fromDate,
            @RequestParam(required = false) Date toDate, @RequestParam(required = false) Long fromMills,
            @RequestParam(required = false) Long toMills, Authentication auth) {
        logger.info("Getting params: email: {}, mqttId: {}, auth: {}", email, mqttId, auth);

        Pair<Boolean, String> access = processUserAccess(email, auth);
        if (access.getFirst()) {
            email = access.getSecond();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(access.getSecond());
        }

        logger.info("Updated email from authorization: {}", email);

        ObjectNode node = mapper.createObjectNode();
        Long count;

        // Currently only support both of the parameters provided
        if (fromMills == null || toMills == null) {
            if (fromDate == null || toDate == null) {

                if (mqttId == null) {
                    count = messageRepository.countByEmail(email);
                } else {
                    count = messageRepository.countByMqttId(mqttId);
                }
            } else {
                if (mqttId == null) {
                    count = messageRepository.countByEmailAndDateBetween(email, fromDate, toDate);
                } else {
                    count = messageRepository.countByMqttIdAndDateBetween(mqttId, fromDate, toDate);
                }
            }
        } else {
            if (mqttId == null) {
                count = messageRepository.countByEmailAndDateBetween(email, new Date(fromMills), new Date(toMills));
            } else {
                count = messageRepository.countByMqttIdAndDateBetween(mqttId, new Date(fromMills), new Date(toMills));
            }
        }
        node.put("count", count);
        return ResponseEntity.status(HttpStatus.OK).body(node);
    }

    private static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-DD hh:mm:ss");

    // http://localhost:8080/api/message/count?mqttId=device0000&fromMills=1624666885920&toMills=1624684885920
    @GetMapping("/detailcount")
    public ResponseEntity<?> detailcount(@RequestParam(required = false) String email, @RequestParam long fromMills,
            @RequestParam long toMills, @RequestParam(required = false, defaultValue = "1") Integer tick,
            Authentication auth) {

        Pair<Boolean, String> access = processUserAccess(email, auth);
        if (access.getFirst()) {
            email = access.getSecond();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(access.getSecond());
        }

        logger.info("Updated email from authorization: {}", email);

        // ObjectNode node = mapper.createObjectNode();
        ArrayNode array = mapper.createArrayNode();

        // Currently only support both of the parameters provided

        logger.info("Counting from {} to {} with tick: {}", fromMills, toMills, tick);

        long spand = toMills - fromMills;
        long interval = spand / tick;

        if (spand <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pls provide a valid fromMills and toMills");
        } else if (interval <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid time interval, are you providing a negative tick?");
        } else {
            List<Device> devices = deviceRepository.findByEmail(email);

            for (Device device : devices) {
                ObjectNode deviceNode = mapper.createObjectNode();
                deviceNode.put("id", device.getMqttId());
                ArrayNode data = mapper.createArrayNode();
                deviceNode.set("data", data);

                for (int i = 0; i < tick; i++) {
                    long start = fromMills + i * interval;
                    long end = start + interval;
                    Date from = new Date(start);
                    Date to = new Date(end);
                    long count = messageRepository.countByMqttIdAndDateBetween(device.getMqttId(), from, to);
                    ObjectNode node = mapper.createObjectNode();
                    node.put("x", fmt.format(from));
                    node.put("y", count);
                    data.add(node);
                }

                array.add(deviceNode);
            }

            return ResponseEntity.status(HttpStatus.OK).body(array);
        }

    }
}
