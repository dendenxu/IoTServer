package com.neoncubes.iotserver;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "user", path = "user")
public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByLastName(@Param("name") String name);

    List<User> findByFirstName(@Param("name") String name);

    User findByEmail(@Param("email") String email);
}
