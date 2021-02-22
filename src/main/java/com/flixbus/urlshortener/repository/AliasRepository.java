package com.flixbus.urlshortener.repository;

import com.flixbus.urlshortener.model.Alias;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AliasRepository extends MongoRepository<Alias, String> {
    Alias findFirstByUsed(Boolean used);
    boolean existsByKey(String key);
    long countByUsed(Boolean used);
}
