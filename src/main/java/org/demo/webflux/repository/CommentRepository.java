package org.demo.webflux.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.demo.webflux.domain.Comment;

public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {

    Flux<Comment> findAllByBook_Id(String bookId);

    Mono<Void> deleteAllByBook_Id(String bookId);

}
