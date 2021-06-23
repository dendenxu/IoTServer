package com.neoncubes.iotserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

@Configuration
public class MqttReceiver {
    private static final Logger logger = LoggerFactory.getLogger(MqttReceiver.class);

    @Bean
    public MqttClient mqttClientBean(@Value("${mqtt.clientId}") String clientId,
            @Value("${mqtt.hostname}") String hostname, @Value("${mqtt.port}") int port) throws MqttException {

        logger.info("Creating the mqtt client {} with {}:{}", clientId, hostname, port);

        MqttClientPersistence persistence = new MemoryPersistence();

        logger.info("Created persistence: {}", persistence);

        MqttClient mqttClient = new MqttClient("tcp://" + hostname + ":" + port, clientId, persistence);

        mqttClient.connect(mqttConnectOptions());

        return mqttClient;
    }

    @Bean
    @ConfigurationProperties(prefix = "mqtt")
    public MqttConnectOptions mqttConnectOptions() {
        return new MqttConnectOptions();
    }
}
