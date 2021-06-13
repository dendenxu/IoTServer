package com.neoncubes.iotserver;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//@Component
public class IoTUserPrincipal implements UserDetails {
    private final User user;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public IoTUserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO Auto-generated method stub
        logger.info("Getting authorities");
        return Collections.singletonList(new SimpleGrantedAuthority(this.user.getRole()));
    }

    @Override
    public String getPassword() {
        logger.info("Getting password for {}", this.user);
        // TODO Auto-generated method stub
//        return (new BCryptPasswordEncoder()).encode(this.user.getPassword());
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        logger.info("Getting user name for {}", this.user);
        // TODO Auto-generated method stub
        return this.user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return true;
    }
}