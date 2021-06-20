package com.neoncubes.iotserver;

// This can be used to create a long integer value that can be updated atomically

import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Autowired
    private UserRepository userRepository;

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
            @RequestParam(required = false) String name, Authentication auth) {
        logger.info("Getting params: email: {}, name: {}, auth: {}", email, name, auth);

        Pair<Boolean, String> access = processUserAccess(email, auth);
        if (access.getFirst()) {
            email = access.getSecond();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(access.getSecond());
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot find the user specified");
        } else {
            if (name == null) {
                return ResponseEntity.status(HttpStatus.OK).body(deviceRepository.findByUser(user));
            } else {
                Device device = deviceRepository.findByNameAndUser(name, user);
                if (device == null) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot find the device specified");
                } else {
                    return ResponseEntity.status(HttpStatus.OK).body(device);
                }
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

        User user = userRepository.findByEmail(email);
        if (deviceRepository.findByNameAndUser(device.getName(), user) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The device already exists.");
        } else {
            deviceRepository.save(device);
            return ResponseEntity.status(HttpStatus.OK).body("OK, the server has remembered this device.");
        }
    }

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

        User user = userRepository.findByEmail(email);
        if (deviceRepository.findByMqttIdAndUser(device.getMqttId(), user) == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The device doesn't exists.");
        } else {
            deviceRepository.save(device);
            return ResponseEntity.status(HttpStatus.OK).body("OK, the server has remembered the new device.");
        }
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

        User user = userRepository.findByEmail(email);
        if (deviceRepository.findByMqttIdAndUser(device.getMqttId(), user) == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The device doesn't exists.");
        } else {
            deviceRepository.delete(device);
            return ResponseEntity.status(HttpStatus.OK).body("OK, the server has deleted the new device.");
        }
    }
}
