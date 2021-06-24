package com.neoncubes.iotserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
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

    @Bean
    public ObjectMapper mapperBean(IoTMessageDeserializer deserializer) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(IoTMessage.class, deserializer);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);

        return mapper;
    }

    @Lazy
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private IoTMessageRepository repo;

    public void publish(final String topic, final String payload, int qos, boolean retained) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(payload.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);

        mqttClient.publish(topic, mqttMessage);
        // mqttClient.publish(topic, payload.getBytes(), qos, retained);
        // mqttClient.disconnect();
    }

    public void subscribe(final String topic) throws MqttException, InterruptedException {
        logger.info("[MQTT] Messages received:");
        mqttClient.subscribeWithResponse(topic, (responseTopic, message) -> {
            // int id = message.getId();
            String payload = new String(message.getPayload());
            try {
                IoTMessage msg = mapper.readValue(payload, IoTMessage.class);

                if (msg.getDevice() != null) {
                    logger.debug("Deserialized IoTMessage: {}", msg);
                    // ! only messages with correpsonding device in DB will be saved
                    repo.save(msg); // will be print an error?
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        mqttClient.disconnect();
        mqttClient.close();
        logger.info("Destroying the MQTT bean");
    }
}
