package com.moviid.proxy.mongodb.repository;

import com.moviid.bean.MoviidGrappe;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VideoSectionsRepository extends MongoRepository<MoviidGrappe,String> {
}
