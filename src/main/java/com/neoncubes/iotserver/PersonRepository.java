package com.neoncubes.iotserver;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

// @RepositoryRestResource is not required for a repository to be exported.
// It is used only to change the export details
@RepositoryRestResource(collectionResourceRel = "people", path = "people")

// MongoRepository implements PagingAndSortingRepository
/**
 * Domain repositories extending this interface can selectively expose CRUD
 * methods by simply declaring methods of the same signature as those declared
 * in {@link CrudRepository}.
 * 
 * @see CrudRepository
 * @param <T>  the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 */
public interface PersonRepository extends MongoRepository<Person, String> {
    List<Person> findByLastName(@Param("name") String name);

    List<Person> findByFirstName(@Param("name") String name);
}
