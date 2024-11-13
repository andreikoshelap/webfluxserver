package org.demo.webflux.web;

import org.demo.webflux.dto.list.AuthorListDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import org.demo.webflux.domain.Author;
import org.demo.webflux.dto.AuthorDto;
import org.demo.webflux.dto.ErrorDto;
import org.demo.webflux.repository.AuthorRepository;
import org.demo.webflux.repository.BookRepository;
import org.demo.webflux.service.impl.mapping.MappingService;

import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.AUTHOR_NOT_FOUND;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.AUTHOR_RELATED_ENTITY;

@Configuration
public class AuthorFunctionalConfig {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final MappingService mappingService;

    public AuthorFunctionalConfig(AuthorRepository authorRepository, BookRepository bookRepository, MappingService mappingService) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.mappingService = mappingService;
    }

    @Bean
    RouterFunction<ServerResponse> getAllAuthorsRoute() {
        return route()
                .GET("/api/author/", accept(APPLICATION_JSON),
                        request -> ok().body(
                                authorRepository.findAll()
                                        .collect(Collectors.toList())
                                        .map(authors -> AuthorListDto.builder()
                                                .authors(mappingService.mapAsList(authors, AuthorDto.class))
                                                .build()), AuthorListDto.class)
                ).build();
    }

    @Bean
    RouterFunction<ServerResponse> deleteAuthorByIdRoute() {
        return route()
                .DELETE("/api/author/{id}", accept(APPLICATION_JSON),
                        request -> authorRepository.existsById(request.pathVariable("id"))
                                .flatMap(isExistAuthor -> {
                                    if (isExistAuthor) {
                                        return bookRepository.existsByAuthor_Id(request.pathVariable("id"))
                                                .flatMap(isExistBook -> {
                                                    if (isExistBook) {
                                                        return ServerResponse.status(404).body(Mono.just(ErrorDto.builder()
                                                                .code(AUTHOR_RELATED_ENTITY.getCode())
                                                                .message(AUTHOR_RELATED_ENTITY.getMessage())
                                                                .build()), ErrorDto.class);
                                                    } else {
                                                        return authorRepository.deleteById(request.pathVariable("id")).then(ok().build());
                                                    }
                                                });
                                    } else {
                                        return ServerResponse.status(404).body(Mono.just(ErrorDto.builder()
                                                .code(AUTHOR_NOT_FOUND.getCode())
                                                .message(AUTHOR_NOT_FOUND.getMessage())
                                                .build()), ErrorDto.class);
                                    }
                                })).build();
    }

    @Bean
    RouterFunction<ServerResponse> saveAuthorRoute() {
        return route()
                .POST("/api/author/", accept(APPLICATION_JSON),
                        req -> req.body(toMono(AuthorDto.class))
                                .flatMap(authorDto -> {
                                    if (authorDto.getId() != null) {
                                        return authorRepository.findById(authorDto.getId())
                                                .flatMap(existedAuthor -> authorRepository.save(Author.builder()
                                                        .id(existedAuthor.getId())
                                                        .firstName(authorDto.getFirstName())
                                                        .lastName(authorDto.getLastName())
                                                        .build())
                                                        .map(savedAuthor -> mappingService.map(savedAuthor, AuthorDto.class))
                                                        .flatMap(savedAuthor ->
                                                                bookRepository.findAllByAuthor_Id(savedAuthor.getId())
                                                                        .collectList()
                                                                        .map(authors -> {
                                                                            authors.forEach(b -> b.setAuthor(mappingService.map(savedAuthor, Author.class)));
                                                                            return authors;
                                                                        })
                                                                        .flatMap(books -> bookRepository.saveAll(books)
                                                                                .then(ok().body(Mono.just(savedAuthor), AuthorDto.class)))))
                                                .switchIfEmpty(ServerResponse.status(404).body(Mono.just(ErrorDto.builder()
                                                        .code(AUTHOR_NOT_FOUND.getCode())
                                                        .message(AUTHOR_NOT_FOUND.getMessage())
                                                        .build()), ErrorDto.class));
                                    }
                                    return authorRepository.save(Author.builder()
                                            .firstName(authorDto.getFirstName())
                                            .lastName(authorDto.getLastName())
                                            .build())
                                            .map(savedAuthor -> mappingService.map(savedAuthor, AuthorDto.class))
                                            .flatMap(savedAuthor -> ok().body(Mono.just(savedAuthor), AuthorDto.class));
                                })).build();
    }

}
