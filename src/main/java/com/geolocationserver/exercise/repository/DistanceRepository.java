package com.geolocationserver.exercise.repository;

import com.geolocationserver.exercise.model.Distance;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface DistanceRepository extends CrudRepository<Distance, Double>{
    List<Distance> findBySourceAndDestination(String source, String destination);
    Distance getDistanceById(long id);
}
