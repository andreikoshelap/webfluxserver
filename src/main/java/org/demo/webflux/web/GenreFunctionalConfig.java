package org.demo.webflux.web;

import org.demo.webflux.dto.list.GenreListDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import org.demo.webflux.domain.Genre;
import org.demo.webflux.dto.ErrorDto;
import org.demo.webflux.dto.GenreDto;
import org.demo.webflux.repository.BookRepository;
import org.demo.webflux.repository.GenreRepository;
import org.demo.webflux.service.impl.mapping.MappingService;

import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.GENRE_NOT_FOUND;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.GENRE_RELATED_ENTITY;

@Configuration
public class GenreFunctionalConfig {

    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;
    private final MappingService mappingService;

    public GenreFunctionalConfig(GenreRepository genreRepository, BookRepository bookRepository, MappingService mappingService) {
        this.genreRepository = genreRepository;
        this.bookRepository = bookRepository;
        this.mappingService = mappingService;
    }

    @Bean
    RouterFunction<ServerResponse> getAllGenresRoute() {
        return route()
                .GET("/api/genre/", accept(APPLICATION_JSON),
                        request -> ok().body(
                                genreRepository.findAll()
                                        .collect(Collectors.toList())
                                        .map(genres -> GenreListDto.builder()
                                                .genres(mappingService.mapAsList(genres, GenreDto.class))
                                                .build()), GenreListDto.class)
                ).build();
    }

    @Bean
    RouterFunction<ServerResponse> deleteGenreByIdRoute() {
        return route()
                .DELETE("/api/genre/{id}", accept(APPLICATION_JSON),
                        request -> genreRepository.existsById(request.pathVariable("id"))
                                .flatMap(isExistGenre -> {
                                    if (isExistGenre) {
                                        return bookRepository.existsByGenre_Id(request.pathVariable("id"))
                                                .flatMap(isExistBook -> {
                                                    if (isExistBook) {
                                                        return ServerResponse.status(404).body(Mono.just(ErrorDto.builder()
                                                                .code(GENRE_RELATED_ENTITY.getCode())
                                                                .message(GENRE_RELATED_ENTITY.getMessage())
                                                                .build()), ErrorDto.class);
                                                    } else {
                                                        return genreRepository.deleteById(request.pathVariable("id")).then(ok().build());
                                                    }
                                                });
                                    } else {
                                        return ServerResponse.status(404).body(Mono.just(ErrorDto.builder()
                                                .code(GENRE_NOT_FOUND.getCode())
                                                .message(GENRE_NOT_FOUND.getMessage())
                                                .build()), ErrorDto.class);
                                    }
                                })).build();
    }

    @Bean
    RouterFunction<ServerResponse> saveGenreRoute() {
        return route()
                .POST("/api/genre/", accept(APPLICATION_JSON),
                        req -> req.body(toMono(GenreDto.class))
                                .flatMap(genreDto -> {
                                    if (genreDto.getId() != null) {
                                        return genreRepository.findById(genreDto.getId())
                                                .flatMap(existedGenre -> genreRepository.save(Genre.builder()
                                                        .id(existedGenre.getId())
                                                        .name(genreDto.getName())
                                                        .build())
                                                        .map(savedGenre -> mappingService.map(savedGenre, GenreDto.class))
                                                        .flatMap(savedGenre ->
                                                                bookRepository.findAllByGenre_Id(savedGenre.getId())
                                                                        .collectList()
                                                                        .map(books -> {
                                                                            books.forEach(b -> b.setGenre(mappingService.map(savedGenre, Genre.class)));
                                                                            return books;
                                                                        })
                                                                        .flatMap(books -> bookRepository.saveAll(books)
                                                                                .then(ok().body(Mono.just(savedGenre), GenreDto.class)))))
                                                .switchIfEmpty(ServerResponse.status(404).body(Mono.just(ErrorDto.builder()
                                                        .code(GENRE_NOT_FOUND.getCode())
                                                        .message(GENRE_NOT_FOUND.getMessage())
                                                        .build()), ErrorDto.class));
                                    }
                                    return genreRepository.save(Genre.builder()
                                            .name(genreDto.getName())
                                            .build())
                                            .map(savedGenre -> mappingService.map(savedGenre, GenreDto.class))
                                            .flatMap(savedGenre -> ok().body(Mono.just(savedGenre), GenreDto.class));
                                })).build();
    }

}
