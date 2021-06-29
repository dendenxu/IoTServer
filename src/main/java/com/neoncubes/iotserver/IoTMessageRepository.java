package com.neoncubes.iotserver;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "message", path = "message")
public interface IoTMessageRepository extends MongoRepository<IoTMessage, String> {
    Page<IoTMessage> findPageByEmailOrderByDateDesc(String email, Pageable pageable);

    Stream<IoTMessage> findStreamByEmailOrderByDateDesc(String email, Pageable pageable);

    Page<IoTMessage> findPageByEmail(String email, Pageable pageable);

    List<IoTMessage> findByEmailOrderByDateDesc(String email);

    List<IoTMessage> findByEmail(String email);

    List<IoTMessage> findByMqttId(String mqttId);

    List<IoTMessage> findByMqttIdAndDateBetweenOrderByDateDesc(String mqttId, Date from, Date to);

    List<IoTMessage> findByEmailAndDateBetweenOrderByDateDesc(String email, Date from, Date to);

    Page<IoTMessage> findPageByEmailAndDateBetweenOrderByDateDesc(String email, Date from, Date to, Pageable pageable);

    IoTMessage findTopByMqttIdOrderByDateDesc(String mqttId);

    Long countByMqttIdAndValueGreaterThan(String mqttId, Integer value);

    Long countByMqttIdAndAlert(String mqttId, Integer alert);

    Long countByMqttId(String mqttId);

    Long countByEmail(String email);

    Long countByMqttIdAndDateBetween(String mqttId, Date from, Date to);

    Long countByEmailAndDateBetween(String mqttId, Date from, Date to);

    // ? can null page request be passed into this?
    Stream<IoTMessage> findStreamByMqttId(String mqttId);

    // ? can null page request be passed into this?
    Stream<IoTMessage> findStreamByEmail(String email);

    // we'll be needing calendar for this user for every day of the year?

    // we'll be needing some message bump char for every device

    // we'll be needing a whole message number bar for every device

    // we'll be needing a data flow chart for every device

}
