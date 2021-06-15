package com.neoncubes.iotserver;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;

@Component
public class MqttReceiver {

    @Autowired
    private MqttClient mqttClient;

    private static final Gson gson = new Gson();

    private static final Logger logger = LoggerFactory.getLogger(MqttReceiver.class);

    public void publish(final String topic, final String payload, int qos, boolean retained)
            throws MqttException {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(payload.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);

        mqttClient.publish(topic, mqttMessage);

        //mqttClient.publish(topic, payload.getBytes(), qos, retained);

        mqttClient.disconnect();
    }

    public void subscribe(final String topic) throws MqttException, InterruptedException {
        logger.info("[MQTT] Messages received:");

        mqttClient.subscribeWithResponse(topic, (responseTopic, message) -> {
            int id = message.getId();
            String payload = new String(message.getPayload());
            logger.info(message.getId() + " -> " + new String(payload));
            IoTMessage msg = gson.fromJson(payload, IoTMessage.class);

            logger.info("Deserialized IoTMessage: {}", msg);
        });
    }
}
