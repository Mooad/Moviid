package com.moviid.proxy.mongodb.repository;

import com.moviid.bean.MoviidGrappe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MoviidGrappeRepository extends MongoRepository<MoviidGrappe, String> {
    Optional<MoviidGrappe> findByJobId(String jobId);
}
