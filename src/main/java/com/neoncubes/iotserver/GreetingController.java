package com.neoncubes.iotserver;

import org.springframework.boot.json.GsonJsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

// This can be used to create a long integer value that can be updated atomically
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController // short hand for @ResponseBody and @Controller
public class GreetingController {

//    // 5位同学的成绩:
//    int[] ns = new int[] { 68, 79, 91, 85, 62 };
//    System.out.println(ns.length); // 编译器自动推算数组大小为5
//    int[] ns = { 68, 79, 91, 85, 62 };

    Logger logger = LoggerFactory.getLogger(GreetingController.class);
    private static final String[] firstNames = { "A&W", "Cloudflare", "Fastly", "YouTube" };

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private PersonRepository repository;

    @GetMapping("/greeting") // GetMapping derived from RequestMapping, so we can also use
                             // @RequestMapping(method=GET)
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        // RequestParam binds the name parameter of this method to the valid of a GET
        // method
        // Returned directly as an object, instead of view
        // This object will be mapped to JSON by MappingJackson2HttpMessageConverter
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    @GetMapping("/addsomenames")
    public Greeting addsomenames(@RequestParam(value = "last", defaultValue = "World") String last) {
        // name is the last name
        // we'll populate it with some first names
        Vector<Person> people = new Vector<Person>();
        for (String first : firstNames) {
            people.add(new Person(first, last));
        }
        repository.insert(people);
        return new Greeting(counter.incrementAndGet(), String.format(template, last));
    }

    @PostMapping("/createaccount")
    public String createAccount(@RequestBody String payload) {
        System.out.println(payload);
        logger.info(payload);
        return "OK?";
    }
}
