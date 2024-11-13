package org.demo.webflux.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.demo.webflux.domain.Book;

public interface BookRepository extends ReactiveMongoRepository<Book, String> {

    Mono<Boolean> existsByGenre_Id(String genreId);

    Mono<Boolean> existsByAuthor_Id(String authorId);

    Flux<Book> findAllByGenre_Id(String genreId);

    Flux<Book> findAllByAuthor_Id(String authorId);

}
