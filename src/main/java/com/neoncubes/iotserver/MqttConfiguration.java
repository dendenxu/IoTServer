package com.neoncubes.iotserver;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MqttConfiguration.class);

    @Bean
    public MqttClient mqttClient(
            @Value("${mqtt.clientId}") String clientId,
            @Value("${mqtt.hostname}") String hostname,
            @Value("${mqtt.port}") int port
            ) throws MqttException {

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
