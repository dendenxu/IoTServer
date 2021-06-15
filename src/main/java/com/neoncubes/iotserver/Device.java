package com.neoncubes.iotserver;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
@CompoundIndex(name = "device_id", def = "{'user.email' : 1, 'name' : 1}")
public class Device implements Serializable {
    @Id
    private String id;
    private String name;
    private String info;
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

    private DeviceType[] type = {DeviceType.Bot};

    @CreatedDate
    private Date createdDate;

    @LastModifiedDate
    private Date lastModifiedDate;

    public Device(String name, String info, Device.DeviceType[] type, User user) {
        this.name = name;
        this.info = info;
        this.type = type;
        this.user = user;
    }

    private boolean recycled;
}
