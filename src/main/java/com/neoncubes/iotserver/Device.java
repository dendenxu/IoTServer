package com.neoncubes.iotserver;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device implements Serializable {
    @Id
    private String mqttId;
    private String desc;
    private String name;
    @DBRef
    private User user;

    @Version
    public Integer version;

    public enum DeviceType {
        Car(0), Bot(1), Drone(2), Monitor(3);

        public final int i;

        private DeviceType(int i) {
            this.i = i;
        }
    }

    private DeviceType[] type = { DeviceType.Bot };

    @CreatedDate
    private Date createdDate;

    @LastModifiedDate
    private Date lastModifiedDate;

    public Device(String mqttId, String name, String desc, Device.DeviceType[] type, User user) {
        this.mqttId = mqttId;
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.user = user;
    }

    private boolean recycled;
}
