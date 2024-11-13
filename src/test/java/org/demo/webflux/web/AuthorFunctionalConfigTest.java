package org.demo.webflux.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.webflux.dto.AuthorDto;
import org.demo.webflux.dto.ErrorDto;
import org.demo.webflux.dto.list.AuthorListDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.demo.webflux.domain.Author;
import org.demo.webflux.domain.Book;
import org.demo.webflux.repository.AuthorRepository;
import org.demo.webflux.repository.BookRepository;
import org.demo.webflux.service.impl.mapping.MappingService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.AUTHOR_NOT_FOUND;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.AUTHOR_RELATED_ENTITY;

@SpringBootTest
@DisplayName("Route for authors should ")
public class AuthorFunctionalConfigTest {

    private static final String AUTHOR_DOMAIN_URL = "/api/author/";
    private static final String AUTHOR_BY_ID_URL = AUTHOR_DOMAIN_URL + "{id}";

    private static final String AUTHOR_ID = "1";
    private static final String AUTHOR_FIRST_NAME = "firstname";
    private static final String AUTHOR_LAST_NAME = "lastname";
    private static final String BOOK_ID = "1";
    private static final String BOOK_TITLE = "test";

    private static WebTestClient testClient;
    private static ObjectMapper mapper;
    private static Author author;
    private static AuthorDto authorDto;
    private static AuthorDto authorDtoWithoutId;
    private static AuthorListDto authorListDto;
    private static ErrorDto errorDtoNotFound;
    private static ErrorDto errorDtoRelatedBook;
    private static Book bookWithAuthor;
    private static Book bookWithAuthorUpdated;

    @Configuration
    @Import(AuthorFunctionalConfig.class)
    public static class AuthorsRouterConfig {
    }

    @Autowired
    @Qualifier("getAllAuthorsRoute")
    private RouterFunction<ServerResponse> getAllAuthorsRoute;

    @Autowired
    @Qualifier("deleteAuthorByIdRoute")
    private RouterFunction<ServerResponse> deleteAuthorByIdRoute;

    @Autowired
    @Qualifier("saveAuthorRoute")
    private RouterFunction<ServerResponse> saveAuthorRoute;

    @MockBean
    private AuthorRepository authorRepository;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private MappingService mappingService;


    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
        author = Author.builder()
                .id(AUTHOR_ID)
                .firstName(AUTHOR_FIRST_NAME)
                .lastName(AUTHOR_LAST_NAME)
                .build();
        authorDto = AuthorDto.builder()
                .id(AUTHOR_ID)
                .firstName(AUTHOR_FIRST_NAME)
                .lastName(AUTHOR_LAST_NAME)
                .build();
        authorDtoWithoutId = AuthorDto.builder()
                .firstName(AUTHOR_FIRST_NAME)
                .lastName(AUTHOR_LAST_NAME)
                .build();
        authorListDto = AuthorListDto.builder()
                .authors(List.of(authorDto))
                .build();
        errorDtoNotFound = ErrorDto.builder()
                .code(AUTHOR_NOT_FOUND.getCode())
                .message(AUTHOR_NOT_FOUND.getMessage())
                .build();
        errorDtoRelatedBook = ErrorDto.builder()
                .code(AUTHOR_RELATED_ENTITY.getCode())
                .message(AUTHOR_RELATED_ENTITY.getMessage())
                .build();
        bookWithAuthor = Book.builder()
                .id(BOOK_ID)
                .title(BOOK_TITLE)
                .author(Author.builder()
                        .id(AUTHOR_ID)
                        .firstName("lala")
                        .lastName("lulu")
                        .build())
                .build();
        bookWithAuthorUpdated = Book.builder()
                .id(BOOK_ID)
                .title(BOOK_TITLE)
                .author(Author.builder()
                        .id(AUTHOR_ID)
                        .firstName(AUTHOR_FIRST_NAME)
                        .lastName(AUTHOR_LAST_NAME)
                        .build())
                .build();
    }

    @Test
    @DisplayName("return all authors")
    public void shouldReturnAllAuthors() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(getAllAuthorsRoute).build();

        when(authorRepository.findAll()).thenReturn(Flux.just(author));
        when(mappingService.mapAsList(List.of(author), AuthorDto.class)).thenReturn(List.of(authorDto));

        testClient.get()
                .uri(AUTHOR_DOMAIN_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(authorListDto));
    }

    @Test
    @DisplayName("delete author by id")
    public void shouldDeleteAuthor() {
        testClient = WebTestClient.bindToRouterFunction(deleteAuthorByIdRoute).build();

        when(authorRepository.existsById(AUTHOR_ID)).thenReturn(Mono.just(true));
        when(bookRepository.existsByAuthor_Id(AUTHOR_ID)).thenReturn(Mono.just(false));
        when(authorRepository.deleteById(AUTHOR_ID)).thenReturn(Mono.empty());

        testClient.delete()
                .uri(uriBuilder -> uriBuilder.path(AUTHOR_BY_ID_URL).build(AUTHOR_ID))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @DisplayName("not delete author by id because author doesn't exist")
    public void shouldNotDeleteAuthorBecauseAuthorDoesNotExist() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(deleteAuthorByIdRoute).build();

        when(authorRepository.existsById(AUTHOR_ID)).thenReturn(Mono.just(false));

        testClient.delete()
                .uri(uriBuilder -> uriBuilder.path(AUTHOR_BY_ID_URL).build(AUTHOR_ID))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(errorDtoNotFound));
    }

    @Test
    @DisplayName("not delete author by id because author has related book")
    public void shouldNotDeleteAuthorBecauseAuthorHasRelatedBook() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(deleteAuthorByIdRoute).build();

        when(authorRepository.existsById(AUTHOR_ID)).thenReturn(Mono.just(true));
        when(bookRepository.existsByAuthor_Id(AUTHOR_ID)).thenReturn(Mono.just(true));

        testClient.delete()
                .uri(uriBuilder -> uriBuilder.path(AUTHOR_BY_ID_URL).build(AUTHOR_ID))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(errorDtoRelatedBook));
    }

    @Test
    @DisplayName("should create new author")
    public void shouldCreateAuthor() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(saveAuthorRoute).build();

        when(authorRepository.save(Author.builder()
                .firstName(authorDtoWithoutId.getFirstName())
                .lastName(authorDtoWithoutId.getLastName())
                .build())).thenReturn(Mono.just(author));
        when(mappingService.map(author, AuthorDto.class)).thenReturn(authorDto);

        testClient.post()
                .uri(AUTHOR_DOMAIN_URL)
                .bodyValue(authorDtoWithoutId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(authorDto));
    }

    @Test
    @DisplayName("should update existed author")
    public void shouldUpdateAuthor() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(saveAuthorRoute).build();

        when(authorRepository.findById(authorDto.getId())).thenReturn(Mono.just(author));
        when(authorRepository.save(Author.builder()
                .id(author.getId())
                .firstName(authorDto.getFirstName())
                .lastName(authorDto.getLastName())
                .build())).thenReturn(Mono.just(author));
        when(mappingService.map(author, AuthorDto.class)).thenReturn(authorDto);
        when(bookRepository.findAllByAuthor_Id(authorDto.getId())).thenReturn(Flux.just(bookWithAuthor));
        when(mappingService.map(authorDto, Author.class)).thenReturn(author);
        when(bookRepository.saveAll(List.of(bookWithAuthorUpdated))).thenReturn(Flux.just(bookWithAuthorUpdated));

        testClient.post()
                .uri(AUTHOR_DOMAIN_URL)
                .bodyValue(authorDto)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(authorDto));
    }

    @Test
    @DisplayName("should not update existed author because id incorrect")
    public void shouldNotUpdateAuthor() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(saveAuthorRoute).build();

        when(authorRepository.findById(authorDto.getId())).thenReturn(Mono.empty());

        testClient.post()
                .uri(AUTHOR_DOMAIN_URL)
                .bodyValue(authorDto)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(errorDtoNotFound));
    }

}
