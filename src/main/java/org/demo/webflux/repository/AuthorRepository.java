package org.demo.webflux.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.demo.webflux.domain.Author;

public interface AuthorRepository extends ReactiveMongoRepository<Author, String> {
}
