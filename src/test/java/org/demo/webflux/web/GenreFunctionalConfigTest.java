package org.demo.webflux.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.webflux.dto.ErrorDto;
import org.demo.webflux.dto.GenreDto;
import org.demo.webflux.dto.list.GenreListDto;
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
import org.demo.webflux.domain.Book;
import org.demo.webflux.domain.Genre;
import org.demo.webflux.repository.BookRepository;
import org.demo.webflux.repository.GenreRepository;
import org.demo.webflux.service.impl.mapping.MappingService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.GENRE_NOT_FOUND;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.GENRE_RELATED_ENTITY;

@SpringBootTest
@DisplayName("Route for genres should ")
public class GenreFunctionalConfigTest {

    private static final String GENRE_DOMAIN_URL = "/api/genre/";
    private static final String GENRE_BY_ID_URL = GENRE_DOMAIN_URL + "{id}";

    private static final String GENRE_ID = "1";
    private static final String GENRE_NAME = "name";
    private static final String BOOK_ID = "1";
    private static final String BOOK_TITLE = "test";

    private static WebTestClient testClient;
    private static ObjectMapper mapper;
    private static Genre genre;
    private static GenreDto genreDto;
    private static GenreDto genreDtoWithoutId;
    private static GenreListDto genreListDto;
    private static ErrorDto errorDtoNotFound;
    private static ErrorDto errorDtoRelatedBook;
    private static Book bookWithGenre;
    private static Book bookWithGenreUpdated;

    @Configuration
    @Import(GenreFunctionalConfig.class)
    public static class GenresRouterConfig {
    }

    @Autowired
    @Qualifier("getAllGenresRoute")
    private RouterFunction<ServerResponse> getAllGenresRoute;

    @Autowired
    @Qualifier("deleteGenreByIdRoute")
    private RouterFunction<ServerResponse> deleteGenreByIdRoute;

    @Autowired
    @Qualifier("saveGenreRoute")
    private RouterFunction<ServerResponse> saveGenreRoute;

    @MockBean
    private GenreRepository genreRepository;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private MappingService mappingService;


    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
        genre = Genre.builder()
                .id(GENRE_ID)
                .name(GENRE_NAME)
                .build();
        genreDto = GenreDto.builder()
                .id(GENRE_ID)
                .name(GENRE_NAME)
                .build();
        genreDtoWithoutId = GenreDto.builder()
                .name(GENRE_NAME)
                .build();
        genreListDto = GenreListDto.builder()
                .genres(List.of(genreDto))
                .build();
        errorDtoNotFound = ErrorDto.builder()
                .code(GENRE_NOT_FOUND.getCode())
                .message(GENRE_NOT_FOUND.getMessage())
                .build();
        errorDtoRelatedBook = ErrorDto.builder()
                .code(GENRE_RELATED_ENTITY.getCode())
                .message(GENRE_RELATED_ENTITY.getMessage())
                .build();
        bookWithGenre = Book.builder()
                .id(BOOK_ID)
                .title(BOOK_TITLE)
                .genre(Genre.builder()
                        .id(GENRE_ID)
                        .name("lala")
                        .build())
                .build();
        bookWithGenreUpdated = Book.builder()
                .id(BOOK_ID)
                .title(BOOK_TITLE)
                .genre(Genre.builder()
                        .id(GENRE_ID)
                        .name(GENRE_NAME)
                        .build())
                .build();
    }

    @Test
    @DisplayName("return all genres")
    public void shouldReturnAllGenres() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(getAllGenresRoute).build();

        when(genreRepository.findAll()).thenReturn(Flux.just(genre));
        when(mappingService.mapAsList(List.of(genre), GenreDto.class)).thenReturn(List.of(genreDto));

        testClient.get()
                .uri(GENRE_DOMAIN_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(genreListDto));
    }

    @Test
    @DisplayName("delete genre by id")
    public void shouldDeleteGenre() {
        testClient = WebTestClient.bindToRouterFunction(deleteGenreByIdRoute).build();

        when(genreRepository.existsById(GENRE_ID)).thenReturn(Mono.just(true));
        when(bookRepository.existsByGenre_Id(GENRE_ID)).thenReturn(Mono.just(false));
        when(genreRepository.deleteById(GENRE_ID)).thenReturn(Mono.empty());

        testClient.delete()
                .uri(uriBuilder -> uriBuilder.path(GENRE_BY_ID_URL).build(GENRE_ID))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @DisplayName("not delete genre by id because genre doesn't exist")
    public void shouldNotDeleteGenreBecauseGenreDoesNotExist() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(deleteGenreByIdRoute).build();

        when(genreRepository.existsById(GENRE_ID)).thenReturn(Mono.just(false));

        testClient.delete()
                .uri(uriBuilder -> uriBuilder.path(GENRE_BY_ID_URL).build(GENRE_ID))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(errorDtoNotFound));
    }

    @Test
    @DisplayName("not delete genre by id because genre has related book")
    public void shouldNotDeleteGenreBecauseGenreHasRelatedBook() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(deleteGenreByIdRoute).build();

        when(genreRepository.existsById(GENRE_ID)).thenReturn(Mono.just(true));
        when(bookRepository.existsByGenre_Id(GENRE_ID)).thenReturn(Mono.just(true));

        testClient.delete()
                .uri(uriBuilder -> uriBuilder.path(GENRE_BY_ID_URL).build(GENRE_ID))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(errorDtoRelatedBook));
    }

    @Test
    @DisplayName("should create new genre")
    public void shouldCreateGenre() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(saveGenreRoute).build();

        when(genreRepository.save(Genre.builder()
                .name(genreDtoWithoutId.getName())
                .build())).thenReturn(Mono.just(genre));
        when(mappingService.map(genre, GenreDto.class)).thenReturn(genreDto);

        testClient.post()
                .uri(GENRE_DOMAIN_URL)
                .bodyValue(genreDtoWithoutId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(genreDto));
    }

    @Test
    @DisplayName("should update existed genre")
    public void shouldUpdateGenre() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(saveGenreRoute).build();

        when(genreRepository.findById(genreDto.getId())).thenReturn(Mono.just(genre));
        when(genreRepository.save(Genre.builder()
                .id(genre.getId())
                .name(genreDto.getName())
                .build())).thenReturn(Mono.just(genre));
        when(mappingService.map(genre, GenreDto.class)).thenReturn(genreDto);
        when(bookRepository.findAllByGenre_Id(genreDto.getId())).thenReturn(Flux.just(bookWithGenre));
        when(mappingService.map(genreDto, Genre.class)).thenReturn(genre);
        when(bookRepository.saveAll(List.of(bookWithGenreUpdated))).thenReturn(Flux.just(bookWithGenreUpdated));

        testClient.post()
                .uri(GENRE_DOMAIN_URL)
                .bodyValue(genreDto)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(genreDto));
    }

    @Test
    @DisplayName("should not update existed genre because id incorrect")
    public void shouldNotUpdateGenre() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(saveGenreRoute).build();

        when(genreRepository.findById(genreDto.getId())).thenReturn(Mono.empty());

        testClient.post()
                .uri(GENRE_DOMAIN_URL)
                .bodyValue(genreDto)
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
