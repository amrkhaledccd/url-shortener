package com.flixbus.urlshortener.repository;

import com.flixbus.urlshortener.model.TinyUrl;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TinyUrlRepository extends MongoRepository<TinyUrl, String> {
    Optional<TinyUrl> findByAlias(String alias);
}
