package com.neoncubes.iotserver;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Component
public class IoTMessageDeserializer extends JsonDeserializer<IoTMessage> {

    // private static final Logger logger = LoggerFactory.getLogger(IoTMessageDeserializer.class);

    @Autowired
    private DeviceRepository repo;

    private final ObjectMapper mapper = new ObjectMapper();

    // public IoTMessageDeserializer() {
    // logger.info("Constructing...");
    // // Mqtt start separate thread?
    // // So no context will be actually found
    // SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    // }

    @Override
    public IoTMessage deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        IoTMessage msg = new IoTMessage();

        ClientMessage clientMessage = mapper.readValue(jsonParser, ClientMessage.class);
        // ClientMessage clientMessage = new ClientMessage();
        msg.setDevice(repo.findByMqttId(clientMessage.clientId));
        Timestamp stamp = new Timestamp(clientMessage.timestamp);
        Date date = new Date(stamp.getTime());
        msg.setDate(date);
        msg.setInfo(clientMessage.info);
        msg.setValue(clientMessage.value);
        msg.setAlert(clientMessage.alert);
        msg.setLng(clientMessage.lng);
        msg.setLat(clientMessage.lat);

        return msg;
    }

    // Reference:
    @Data
    public static class ClientMessage implements Serializable {
        // 设备ID
        private String clientId;
        // 上报信息
        private String info;
        // 设备数据
        private int value;
        // 是否告警，0-正常，1-告警
        private int alert;
        // 设备位置，经度
        private double lng;
        // 设备位置，纬度
        private double lat;
        // 上报时间，ms
        private long timestamp;
    }

}