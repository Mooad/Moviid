package com.moviid.translator.dao;

import com.moviid.translator.bean.mongodb.MoviidGrappe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MoviidGrappeRepository extends MongoRepository<MoviidGrappe, String> {
    Optional<MoviidGrappe> findByJobId(String jobId);
}
