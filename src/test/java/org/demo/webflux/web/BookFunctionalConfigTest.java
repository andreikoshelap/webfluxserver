package org.demo.webflux.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.webflux.dto.AuthorDto;
import org.demo.webflux.dto.BookDto;
import org.demo.webflux.dto.ErrorDto;
import org.demo.webflux.dto.GenreDto;
import org.demo.webflux.dto.list.BookListDto;
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
import org.demo.webflux.domain.Genre;
import org.demo.webflux.repository.AuthorRepository;
import org.demo.webflux.repository.BookRepository;
import org.demo.webflux.repository.CommentRepository;
import org.demo.webflux.repository.GenreRepository;
import org.demo.webflux.service.impl.mapping.MappingService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.BOOK_NOT_FOUND;

@SpringBootTest
@DisplayName("Route for books should ")
public class BookFunctionalConfigTest {

    private static final String BOOK_DOMAIN_URL = "/api/book/";
    private static final String BOOK_BY_ID_URL = BOOK_DOMAIN_URL + "{id}";

    private static final String BOOK_ID = "1";
    private static final String BOOK_TITLE = "test";
    private static final String AUTHOR_ID = "1";
    private static final String AUTHOR_FIRST_NAME = "firstname";
    private static final String AUTHOR_LAST_NAME = "lastname";
    private static final String GENRE_ID = "1";
    private static final String GENRE_NAME = "name";

    private static WebTestClient testClient;
    private static ObjectMapper mapper;
    private static Book book;
    private static Genre genre;
    private static Author author;
    private static BookDto bookDto;
    private static BookDto bookDtoWithoutId;
    private static BookListDto bookListDto;
    private static ErrorDto errorDtoNotFound;

    @Configuration
    @Import(BookFunctionalConfig.class)
    public static class BookRouterConfig {
    }

    @Autowired
    @Qualifier("getAllBooksRoute")
    private RouterFunction<ServerResponse> getAllBooksRoute;

    @Autowired
    @Qualifier("deleteBookByIdRoute")
    private RouterFunction<ServerResponse> deleteBookByIdRoute;

    @Autowired
    @Qualifier("saveBookRoute")
    private RouterFunction<ServerResponse> saveBookRoute;

    @MockBean
    private GenreRepository genreRepository;

    @MockBean
    private AuthorRepository authorRepository;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private MappingService mappingService;

    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
        genre = Genre.builder()
                .id(GENRE_ID)
                .name(GENRE_NAME)
                .build();
        GenreDto genreDto = GenreDto.builder()
                .id(GENRE_ID)
                .name(GENRE_NAME)
                .build();
        author = Author.builder()
                .id(AUTHOR_ID)
                .firstName(AUTHOR_FIRST_NAME)
                .lastName(AUTHOR_LAST_NAME)
                .build();
        AuthorDto authorDto = AuthorDto.builder()
                .id(AUTHOR_ID)
                .firstName(AUTHOR_FIRST_NAME)
                .lastName(AUTHOR_LAST_NAME)
                .build();
        book = Book.builder()
                .id(BOOK_ID)
                .title(BOOK_TITLE)
                .genre(genre)
                .author(author)
                .build();
        bookDto = BookDto.builder()
                .id(BOOK_ID)
                .title(BOOK_TITLE)
                .genre(genreDto)
                .author(authorDto)
                .build();
        bookDtoWithoutId = BookDto.builder()
                .title(BOOK_TITLE)
                .genre(genreDto)
                .author(authorDto)
                .build();
        bookListDto = BookListDto.builder()
                .books(List.of(bookDto))
                .build();
        errorDtoNotFound = ErrorDto.builder()
                .code(BOOK_NOT_FOUND.getCode())
                .message(BOOK_NOT_FOUND.getMessage())
                .build();
    }

    @Test
    @DisplayName("return all books")
    public void shouldReturnAllBooks() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(getAllBooksRoute).build();

        when(bookRepository.findAll()).thenReturn(Flux.just(book));
        when(mappingService.mapAsList(List.of(book), BookDto.class)).thenReturn(List.of(bookDto));

        testClient.get()
                .uri(BOOK_DOMAIN_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(bookListDto));
    }

    @Test
    @DisplayName("delete book by id")
    public void shouldDeleteBook() {
        testClient = WebTestClient.bindToRouterFunction(deleteBookByIdRoute).build();

        when(bookRepository.existsById(BOOK_ID)).thenReturn(Mono.just(true));
        when(bookRepository.deleteById(BOOK_ID)).thenReturn(Mono.empty());
        when(commentRepository.deleteAllByBook_Id(BOOK_ID)).thenReturn(Mono.empty());

        testClient.delete()
                .uri(uriBuilder -> uriBuilder.path(BOOK_BY_ID_URL).build(BOOK_ID))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @DisplayName("not delete book by id because book doesn't exist")
    public void shouldNotDeleteBook() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(deleteBookByIdRoute).build();

        when(bookRepository.existsById(BOOK_ID)).thenReturn(Mono.just(false));

        testClient.delete()
                .uri(uriBuilder -> uriBuilder.path(BOOK_BY_ID_URL).build(BOOK_ID))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .json(mapper.writeValueAsString(errorDtoNotFound));
    }

    @Test
    @DisplayName("should create new book")
    public void shouldCreateBook() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(saveBookRoute).build();

        when(genreRepository.findById(GENRE_ID)).thenReturn(Mono.just(genre));
        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Mono.just(author));
        when(bookRepository.save(Book.builder()
                .title(bookDtoWithoutId.getTitle())
                .author(author)
                .genre(genre)
                .build())).thenReturn(Mono.just(book));
        when(mappingService.map(book, BookDto.class)).thenReturn(bookDto);

        testClient.post()
                .uri(BOOK_DOMAIN_URL)
                .bodyValue(bookDtoWithoutId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(bookDto));
    }

    @Test
    @DisplayName("should update existed book")
    public void shouldUpdateBook() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(saveBookRoute).build();

        when(bookRepository.findById(BOOK_ID)).thenReturn(Mono.just(book));
        when(genreRepository.findById(GENRE_ID)).thenReturn(Mono.just(genre));
        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Mono.just(author));
        when(bookRepository.save(Book.builder()
                .id(book.getId())
                .title(bookDtoWithoutId.getTitle())
                .author(author)
                .genre(genre)
                .build())).thenReturn(Mono.just(book));
        when(mappingService.map(book, BookDto.class)).thenReturn(bookDto);

        testClient.post()
                .uri(BOOK_DOMAIN_URL)
                .bodyValue(bookDto)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(bookDto));
    }

    @Test
    @DisplayName("should not update existed book because book doesn't exist")
    public void shouldNotUpdateBook() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(saveBookRoute).build();

        when(bookRepository.findById(BOOK_ID)).thenReturn(Mono.empty());

        testClient.post()
                .uri(BOOK_DOMAIN_URL)
                .bodyValue(bookDto)
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
