package com.neoncubes.iotserver;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This is a convenience annotation that adds
 * <p>
 * Configuration Tags class as a source of bean definitions
 * EnableAutoConfiguration Add beans based on classpath ComponentScan Look for
 * other components in the package
 */
@SpringBootApplication
@EnableWebSecurity
public class IoTServerApplication extends WebSecurityConfigurerAdapter implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(IoTServerApplication.class);

    @Autowired
    private IoTUserDetailService userService;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder().encode("password")).roles("ADMIN");
        auth.userDetailsService(userService);
    }

    @Bean
    public EmailPasswordFilter authenticationFilter() throws Exception {
        EmailPasswordFilter authenticationFilter = new EmailPasswordFilter();
        authenticationFilter.setAuthenticationSuccessHandler((req, res, auth) -> res.setStatus(HttpStatus.NO_CONTENT.value()));
        authenticationFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler());
        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/account/login", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationFilter;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(corsFilter(), EmailPasswordFilter.class)
                .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // all URLs are protected, except 'POST /login' so anonymous user can authenticate
                .authorizeRequests()
                .antMatchers("/api/account/**").permitAll()
                .anyRequest().authenticated()

                // 401-UNAUTHORIZED when anonymous user tries to access protected URLs
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.FORBIDDEN))

                // IMPORTANT: IF YOU WANT TO USE YOUR IMPLEMENTATION, DON'T CONFIGURE HERE

                // standard logout that sends 204-NO_CONTENT when logout is OK
                .and()
                .logout()
                .logoutUrl("/api/account/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))

////         add CSRF protection to all URLs
        // use custom filter for the default username password filter
        // add CSRF protection to all URLs
                .and()
//                .cors()
//                .disable()
                .csrf()
                .disable()
//                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        ;
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
        logger.info("Subscribed to mqtt topic: {} using {}", "testapp", receiver);

    }
}
