package com.neoncubes.iotserver;

import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;

@Component
public class IoTMessageDeserializer implements JsonDeserializer<IoTMessage> {

    @Autowired
    private DeviceRepository repo;

    @Override
    public IoTMessage deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        IoTMessage msg = new IoTMessage();

        JsonObject obj = json.getAsJsonObject();
        String mqttId = obj.get("clientid").getAsString();
        msg.setDevice(repo.findByMqttId(mqttId));
        long time = obj.get("timestamp").getAsLong();
        Timestamp stamp = new Timestamp(time);
        msg.setDate(new Date(stamp.getTime()));
        msg.setInfo(obj.get("info").getAsString());
        msg.setValue(obj.get("value").getAsInt());
        msg.setAlert(obj.get("alert").getAsInt());
        msg.setLng(obj.get("lng").getAsDouble());
        msg.setLat(obj.get("lat").getAsDouble());

        return msg;
    }
}

