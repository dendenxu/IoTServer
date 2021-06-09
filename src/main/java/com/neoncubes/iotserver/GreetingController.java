package com.neoncubes.iotserver;

// This can be used to create a long integer value that can be updated atomically
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController // short hand for @ResponseBody and @Controller
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting") // GetMapping derived from RequestMapping, so we can also use @RequestMapping(method=GET)
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) { // RequestParam binds the name parameter of this method to the valid of a GET method
        // Returned directly as an object, instead of view
        // This object will be mapped to JSON by MappingJackson2HttpMessageConverter
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

}
