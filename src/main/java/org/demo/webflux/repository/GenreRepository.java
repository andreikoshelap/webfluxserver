package org.demo.webflux.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.demo.webflux.domain.Genre;

public interface GenreRepository extends ReactiveMongoRepository<Genre, String> {
}
