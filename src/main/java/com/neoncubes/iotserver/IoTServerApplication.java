package com.neoncubes.iotserver;

import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
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

    private static final Logger logger = LoggerFactory.getLogger(IoTServerApplication.class);

    @Bean
    public LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Autowired
    private IoTUserDetailService userService;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder().encode("password")).roles("ADMIN");
        auth.userDetailsService(userService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() // we don't care for CSRF in this example
                .formLogin()
                .loginPage("/signin").loginProcessingUrl("/api/user/signin").permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/signup").permitAll()
                .antMatchers("/api/user/query").permitAll()
                .antMatchers("/api/user/register").permitAll()
                .antMatchers("/static/**").permitAll()
                .anyRequest().authenticated()
                .and();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private PersonRepository repository;

    @Autowired
    private MqttReceiver receiver;

    public static void main(String[] args) {
        SpringApplication.run(IoTServerApplication.class, args);
        logger.info("The main program has started");
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

        receiver.subscribe("testapp");

    }
}
