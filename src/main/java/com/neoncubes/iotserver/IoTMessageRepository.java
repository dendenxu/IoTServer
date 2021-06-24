package com.neoncubes.iotserver;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "message", path = "message")
public interface IoTMessageRepository extends MongoRepository<IoTMessage, String> {
    List<IoTMessage> findByDevice(@Param("device") Device device);

    List<IoTMessage> findByDeviceMqttId(@Param("device.mqttId") String mqttId);

    // @Query(value = "{ 'device' : ?0, $limit: 1 }", sort = "{ 'date' : -1 }")
    IoTMessage findTopByDeviceOrderByDateDesc(@Param("device") Device device);
}
