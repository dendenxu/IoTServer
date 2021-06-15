package com.neoncubes.iotserver;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "device", path = "device")
public interface DeviceRepository extends MongoRepository<Device, String> {
    List<Device> findByType(@Param("type") Device.DeviceType type);

    Device findByMqttId(@Param("mqttId") String mqttId);

    Device findByNameAndUser(@Param("name") String name, @Param("user") User user);

    Device findByMqttIdAndUser(@Param("mqttId") String mqttId, @Param("user") User user);

    List<Device> findByUser(@Param("user") User user);
}
