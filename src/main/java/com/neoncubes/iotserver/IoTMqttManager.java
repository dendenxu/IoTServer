package com.neoncubes.iotserver;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

@Component
public class IoTMqttManager {

    private static final Logger logger = LoggerFactory.getLogger(IoTMqttManager.class);

    @Autowired
    private IoTMessageRepository messageRepository;

    @Autowired
    private MqttReceiver receiver;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @Value("${mqtt.messagetopic}")
    private String messagetopic;
    @Value("${mqtt.lastwilltopic}")
    private String lastwilltopic;
    @Value("${mqtt.connecttopic}")
    private String connecttopic;
    @Value("${mqtt.disconnecttopic}")
    private String disconnecttopic;

    private static interface MessageSaveHandler {
        @Transactional
        void handle(String topic, MqttMessage message) throws Exception;
    }

    private IoTMessage client2server(ClientMessage cli) {
        IoTMessage msg = new IoTMessage();

        // ClientMessage clientMessage = new ClientMessage();
        Device device = deviceRepository.findByMqttId(cli.getClientId());
        msg.setDevice(device);
        if (device != null) {
            msg.setUser(device.getUser());
        }
        Timestamp stamp = new Timestamp(cli.getTimestamp());
        Date date = new Date(stamp.getTime());
        msg.setDate(date);
        msg.setInfo(cli.getInfo());
        msg.setValue(cli.getValue());
        msg.setAlert(cli.getAlert());
        msg.setLng(cli.getLng());
        msg.setLat(cli.getLat());

        return msg;
    }

    public void subscribe() throws MqttException, InterruptedException {

        MessageSaveHandler handler = (topic, message) -> {
            // ! this is transactional
            String payload = new String(message.getPayload());
            logger.info("Receiving payload {} from topic {}", payload, topic);
            try {
                IoTMessage msg = client2server(mapper.readValue(payload, ClientMessage.class));
                Device device = msg.getDevice();
                if (device != null) {
                    // ! these's also an optimistic lock
                    if (topic.equals(this.lastwilltopic) || topic.equals(this.disconnecttopic)) {
                        device.setOnline(false);
                        deviceRepository.save(device);
                        logger.warn("This device disconnects: {}", device);
                    } else if (topic.equals(this.connecttopic)) {
                        device.setOnline(true);
                        deviceRepository.save(device);
                        logger.warn("This device connects: {}", device);
                    }
                    logger.debug("Deserialized IoTMessage: {}", msg);
                    // ! only messages with correpsonding device in DB will be saved
                    messageRepository.save(msg); // will be print an error?
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        IMqttMessageListener handleMessage = (topic, message) -> {
            handler.handle(topic, message);
        };

        receiver.subscribe(messagetopic, handleMessage);
        receiver.subscribe(connecttopic, handleMessage);
        receiver.subscribe(disconnecttopic, handleMessage);
        receiver.subscribe(lastwilltopic, handleMessage);
        logger.info("Subscribed to mqtt topic: {} using {}", "testapp", receiver);
    }
}
