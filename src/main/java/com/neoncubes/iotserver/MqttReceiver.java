package com.neoncubes.iotserver;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class MqttReceiver implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(MqttReceiver.class);

    private MqttClient mqttClient;

    @Autowired
    public MqttReceiver(@Value("${mqtt.clientId}") String clientId, @Value("${mqtt.hostname}") String hostname,
            @Value("${mqtt.port}") int port) throws MqttException {
        this.mqttClient = mqttClient(clientId, hostname, port);
    }

    public MqttClient mqttClient(String clientId, String hostname, int port) throws MqttException {

        logger.info("Creating the mqtt client {} with {}:{}", clientId, hostname, port);

        MqttClientPersistence persistence = new MemoryPersistence();

        logger.info("Created persistence: {}", persistence);

        MqttClient mqttClient = new MqttClient("tcp://" + hostname + ":" + port, clientId, persistence);

        mqttClient.connect(mqttConnectOptions());

        return mqttClient;
    }

    @ConfigurationProperties(prefix = "mqtt")
    public MqttConnectOptions mqttConnectOptions() {
        return new MqttConnectOptions();
    }

    public void publish(final String topic, final String payload, int qos, boolean retained) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(payload.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);

        mqttClient.publish(topic, mqttMessage);
    }

    public void subscribe(final String topic, IMqttMessageListener listener)
            throws MqttException, InterruptedException {
        mqttClient.subscribeWithResponse(topic, listener);
    }

    @Override
    public void destroy() throws Exception {
        mqttClient.disconnect();
        mqttClient.close();
        logger.info("Destroying the MQTT bean");
    }
}
