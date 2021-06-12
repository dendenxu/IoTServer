package com.neoncubes.iotserver;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

public class Initializer extends AbstractHttpSessionApplicationInitializer {

    public Initializer() {
        super(IoTServerApplication.class);
    }

}