package org.demo.webflux.web;

import org.demo.webflux.dto.list.BookListDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import org.demo.webflux.domain.Book;
import org.demo.webflux.dto.BookDto;
import org.demo.webflux.dto.ErrorDto;
import org.demo.webflux.repository.AuthorRepository;
import org.demo.webflux.repository.BookRepository;
import org.demo.webflux.repository.CommentRepository;
import org.demo.webflux.repository.GenreRepository;
import org.demo.webflux.service.impl.mapping.MappingService;

import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.BOOK_NOT_FOUND;

@Configuration
public class BookFunctionalConfig {

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;
    private final CommentRepository commentRepository;
    private final MappingService mappingService;

    public BookFunctionalConfig(BookRepository bookRepository, GenreRepository genreRepository, AuthorRepository authorRepository, CommentRepository commentRepository, MappingService mappingService) {
        this.bookRepository = bookRepository;
        this.genreRepository = genreRepository;
        this.authorRepository = authorRepository;
        this.commentRepository = commentRepository;
        this.mappingService = mappingService;
    }

    @Bean
    RouterFunction<ServerResponse> getAllBooksRoute() {
        return route()
                .GET("/api/book/", accept(APPLICATION_JSON),
                        request -> ok().body(
                                bookRepository.findAll()
                                        .collect(Collectors.toList())
                                        .map(books -> BookListDto.builder()
                                                .books(mappingService.mapAsList(books, BookDto.class))
                                                .build()), BookListDto.class)
                ).build();
    }

    @Bean
    RouterFunction<ServerResponse> deleteBookByIdRoute() {
        return route()
                .DELETE("/api/book/{id}", accept(APPLICATION_JSON),
                        request -> bookRepository.existsById(request.pathVariable("id"))
                                .flatMap(isExistBook -> {
                                    if (isExistBook) {
                                        return bookRepository.deleteById(request.pathVariable("id"))
                                                .zipWith(commentRepository.deleteAllByBook_Id(request.pathVariable("id")))
                                                .then(ok().build());
                                    } else {
                                        return ServerResponse.status(404).body(Mono.just(ErrorDto.builder()
                                                .code(BOOK_NOT_FOUND.getCode())
                                                .message(BOOK_NOT_FOUND.getMessage())
                                                .build()), ErrorDto.class);
                                    }
                                })
                ).build();
    }

    @Bean
    RouterFunction<ServerResponse> saveBookRoute() {
        return route()
                .POST("/api/book/", accept(APPLICATION_JSON),
                        req -> req.body(toMono(BookDto.class))
                                .flatMap(bookDto -> {
                                    if (bookDto.getId() != null) {
                                        return bookRepository.findById(bookDto.getId())
                                                .flatMap(book -> genreRepository.findById(bookDto.getGenre().getId())
                                                        .zipWith(authorRepository.findById(bookDto.getAuthor().getId()))
                                                        .flatMap(tuple -> bookRepository.save(Book.builder()
                                                                .id(book.getId())
                                                                .title(book.getTitle())
                                                                .genre(tuple.getT1())
                                                                .author(tuple.getT2())
                                                                .build())
                                                                .map(savedBook -> mappingService.map(savedBook, BookDto.class))
                                                                .flatMap(savedBook -> ok().body(Mono.just(savedBook), BookDto.class))))
                                                .switchIfEmpty(ServerResponse.status(404).body(Mono.just(new ErrorDto(BOOK_NOT_FOUND.getCode(), BOOK_NOT_FOUND.getMessage())), ErrorDto.class));
                                    }
                                    return genreRepository.findById(bookDto.getGenre().getId())
                                            .zipWith(authorRepository.findById(bookDto.getAuthor().getId()))
                                            .flatMap(tuple -> bookRepository.save(Book.builder()
                                                    .title(bookDto.getTitle())
                                                    .genre(tuple.getT1())
                                                    .author(tuple.getT2())
                                                    .build())
                                                    .map(savedBook -> mappingService.map(savedBook, BookDto.class))
                                                    .flatMap(savedBook -> ok().body(Mono.just(savedBook), BookDto.class)));
                                })).build();
    }

}
