package com.neoncubes.iotserver;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
// import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
@CompoundIndex(def = "{'user': 1, 'date': -1}")
@CompoundIndex(def = "{'device': 1, 'date': -1}")
@CompoundIndex(def = "{'device.mqttId': 1, 'date': -1}")
public class IoTMessage implements Serializable {
    @Id
    private String id;
    // configurable mqtt device for this information, need to retrieve device
    @Indexed
    private String mqttId;
    @Indexed
    private String email;
    // uploaded message content
    private String info;
    // value of the uploaded information
    private int value;
    // 1 for alert and 0 for normal
    private int alert;
    // longitude
    private double lng;
    // latitude
    private double lat;
    // time of this upload, in ms, need to be converted from int
    @Indexed
    private Date date;
}
