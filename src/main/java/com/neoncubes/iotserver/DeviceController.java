package com.neoncubes.iotserver;

import java.util.ArrayList;
// import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

// This can be used to create a long integer value that can be updated atomically

import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
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
import org.springframework.http.HttpStatus;

@RestController // short hand for @ResponseBody and @Controller
@RequestMapping("/api/device")
public class DeviceController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceRepository deviceRepository;

    // @Autowired
    // private UserRepository userRepository;

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

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private IoTMessageRepository messageRepository;

    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam(required = false) String email,
            @RequestParam(required = false) String mqttId, Authentication auth) {
        logger.info("Getting params: email: {}, mqttId: {}, auth: {}", email, mqttId, auth);

        Pair<Boolean, String> access = processUserAccess(email, auth);
        if (access.getFirst()) {
            email = access.getSecond();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(access.getSecond());
        }

        if (mqttId == null) {
            // return
            // ResponseEntity.status(HttpStatus.OK).body(deviceRepository.findByUser(user));
            List<Device> devices = deviceRepository.findByEmailOrderByMqttIdAsc(email);
            ArrayList<ObjectNode> nodes = new ArrayList<ObjectNode>();
            devices.forEach((device) -> {
                IoTMessage message = messageRepository.findTopByMqttIdOrderByDateDesc(device.getMqttId());

                logger.info("Found latest message for this device: {}", message);

                ObjectNode deviceNode = mapper.valueToTree(device);
                ObjectNode node = mapper.createObjectNode();
                node.setAll(deviceNode);
                if (message != null) {
                    ObjectNode messageNode = mapper.valueToTree(message);
                    messageNode.remove("mqttId");
                    node.setAll(messageNode);
                }
                nodes.add(node);
            });
            return ResponseEntity.status(HttpStatus.OK).body(nodes);

        } else {
            Device device = deviceRepository.findByMqttIdAndEmail(mqttId, email);
            if (device == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot find the device specified");
            } else {
                IoTMessage message = messageRepository.findTopByMqttIdOrderByDateDesc(device.getMqttId());

                logger.info("Found lastest message for this device: {}", message);

                ObjectNode deviceNode = mapper.valueToTree(device);
                ObjectNode node = mapper.createObjectNode();
                node.setAll(deviceNode);
                if (message != null) {
                    message.setMqttId(null);
                    ObjectNode messageNode = mapper.valueToTree(message);
                    messageNode.remove("device");
                    node.setAll(messageNode);
                }
                return ResponseEntity.status(HttpStatus.OK).body(node);
            }
        }

    }

    @GetMapping("/query")
    public ResponseEntity<?> query(@RequestParam(required = false) String email,
            @RequestParam(required = false) String mqttId, Authentication auth) {
        logger.info("Getting params: email: {}, mqttId: {}, auth: {}", email, mqttId, auth);

        Pair<Boolean, String> access = processUserAccess(email, auth);
        if (access.getFirst()) {
            email = access.getSecond();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(access.getSecond());
        }

        if (mqttId == null) {
            return ResponseEntity.status(HttpStatus.OK).body(deviceRepository.findByEmailOrderByMqttIdAsc(email));
        } else {
            Device device = deviceRepository.findByMqttIdAndEmail(mqttId, email);
            if (device == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot find the device specified");
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(device);
            }
        }

    }

    @PostMapping("/create")
    public ResponseEntity<?> register(@RequestParam(required = false) String email, @RequestBody Device device,
            Authentication auth) {
        logger.info("The server received this: {}, email: {}, auth: {}", device, email, auth);

        Pair<Boolean, String> access = processUserAccess(email, auth);
        if (access.getFirst()) {
            email = access.getSecond();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(access.getSecond());
        }

        if (deviceRepository.findByMqttId(device.getMqttId()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Device MqttId already taken by other users");
        } else {
            device.setEmail(email);
            deviceRepository.save(device);
            return ResponseEntity.status(HttpStatus.OK).body("OK, the server has remembered this device.");
        }
    }

    private static interface Transaction {
        @Transactional
        Object operate();
    }

    // ! this skips the optimistic locking
    // everything will be rewritten (excluding online/offline status)
    @PatchMapping("/replace")
    public ResponseEntity<?> replace(@RequestParam(required = false) String email, @RequestBody Device device,
            Authentication auth) {
        logger.info("The server received this: {}, email: {}, auth: {}", device, email, auth);

        Pair<Boolean, String> access = processUserAccess(email, auth);
        if (access.getFirst()) {
            email = access.getSecond();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(access.getSecond());
        }

        final String finalEmail = email;

        Transaction tx = () -> {
            Device dbDevice = deviceRepository.findByMqttIdAndEmail(device.getMqttId(), finalEmail);
            if (dbDevice == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("The device doesn't exists. Have you changed MqttId?");
            } else {
                device.setEmail(finalEmail);
                // solve created data being null error
                device.setCreatedDate(dbDevice.getCreatedDate());
                device.setVersion(dbDevice.getVersion());
                device.setOnline(dbDevice.getOnline());
                deviceRepository.save(device);
                return ResponseEntity.status(HttpStatus.OK).body("OK, the server has remembered the new device.");
            }
        };

        return (ResponseEntity<?>) tx.operate();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam(required = false) String email, @RequestBody Device device,
            Authentication auth) {
        logger.info("The server received this: {}, email: {}, auth: {}", device, email, auth);

        Pair<Boolean, String> access = processUserAccess(email, auth);
        if (access.getFirst()) {
            email = access.getSecond();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(access.getSecond());
        }

        if (deviceRepository.findByMqttIdAndEmail(device.getMqttId(), email) == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The device doesn't exists.");
        } else {
            deviceRepository.delete(device);
            return ResponseEntity.status(HttpStatus.OK).body("OK, the server has deleted the new device.");
        }
    }
}
