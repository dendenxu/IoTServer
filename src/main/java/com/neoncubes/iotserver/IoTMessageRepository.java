package com.neoncubes.iotserver;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
// import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "message", path = "message")
public interface IoTMessageRepository extends MongoRepository<IoTMessage, String> {
    List<IoTMessage> findByDevice(@Param("device") Device device);

    @Query(value = "{ 'user.email': ?0 }", fields = "{ 'value' : 1, 'alert' : 1, 'date' : 1, 'lat' : 1, 'lng' : 1, 'info' : 1, 'device.mqttId' : 1 }", sort = "{ 'date': -1 }")
    Page<IoTMessage> findByUserEmail(String email, Pageable pageable);

    @Query(value = "{ 'user.email': ?0 }", fields = "{ 'value' : 1, 'alert' : 1, 'date' : 1, 'lat' : 1, 'lng' : 1, 'info' : 1, 'device.mqttId' : 1 }", sort = "{ 'date': -1 }")
    List<IoTMessage> findByUserEmailDirectList(String email);

    List<IoTMessage> findByDeviceMqttId(@Param("device.mqttId") String mqttId);

    Page<IoTMessage> findByDeviceIn(List<Device> devices, Pageable pageable);

    IoTMessage findTopByDeviceMqttIdOrderByDateDesc(@Param("device.mqttId") String mqttId);
}
