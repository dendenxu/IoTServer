package com.neoncubes.iotserver;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "device", path = "device")
public interface DeviceRepository extends MongoRepository<Device, String> {
    List<Device> findByType(Device.DeviceType type);

    Device findByMqttId(String mqttId);

    Device findByNameAndEmail(String name, String email);

    Device findByMqttIdAndEmail(String mqttId, String email);

    List<Device> findByEmailOrderByMqttIdAsc(String email);

    Integer countByEmail(String email);

    Integer countByOnline(Boolean online);
}
