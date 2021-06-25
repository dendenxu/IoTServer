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
    Page<IoTMessage> findByEmailOrderByDateDesc(String email, Pageable pageable);

    Page<IoTMessage> findByEmail(String email, Pageable pageable);

    List<IoTMessage> findByEmailOrderByDateDesc(String email);

    List<IoTMessage> findByEmail(String email);

    List<IoTMessage> findByMqttId(@Param("device.mqttId") String mqttId);

    IoTMessage findTopByMqttIdOrderByDateDesc(@Param("device.mqttId") String mqttId);
}
