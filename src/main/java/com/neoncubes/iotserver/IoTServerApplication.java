package com.neoncubes.iotserver;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * This is a convenience annotation that adds
 * <p>
 * Configuration Tags class as a source of bean definitions
 * EnableAutoConfiguration Add beans based on classpath ComponentScan Look for
 * other components in the package
 */
@SpringBootApplication
@EnableWebSecurity
@EnableRedisHttpSession // The @EnableRedisHttpSession annotation creates a Spring Bean with the name of
                        // springSessionRepositoryFilter that implements Filter.
public class IoTServerApplication extends WebSecurityConfigurerAdapter implements CommandLineRunner {

    @Bean
    public LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder().encode("password")).roles("ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().and().authorizeRequests().antMatchers("/").hasRole("ADMIN").anyRequest().authenticated();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private PersonRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(IoTServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        repository.deleteAll();

        // save a couple of customers
        repository.save(new Person("Hello", "John"));
        repository.save(new Person("Wired", "Heck"));

        // fetch all customers
        System.out.println("Customers found with findAll");
        for (Person person : repository.findAll()) {
            System.out.println(person);
        }

        System.out.println();

        // fetch an individual customer
        System.out.println("Customer found with findByFirstName('Hello')");
        List<Person> people = repository.findByFirstName("Hello");
        System.out.println(people);

        System.out.println("Customer found with findByLastName('Heck')");
        people = repository.findByLastName("Heck");
        System.out.println(people);

    }
}
