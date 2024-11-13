package org.demo.webflux.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.webflux.dto.CommentDto;
import org.demo.webflux.dto.ErrorDto;
import org.demo.webflux.dto.list.CommentListDto;
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
import org.demo.webflux.domain.Comment;
import org.demo.webflux.repository.BookRepository;
import org.demo.webflux.repository.CommentRepository;
import org.demo.webflux.service.impl.mapping.MappingService;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.BOOK_NOT_FOUND;
import static org.demo.webflux.enumerations.ApplicationErrorsEnum.COMMENT_NOT_FOUND;

@SpringBootTest
@DisplayName("Route for comments should ")
public class CommentFunctionalConfigTest {

    private static final String BOOK_COMMENT_DOMAIN_URL = "/api/book/{id}/comment/";
    private static final String BOOK_COMMENT_BY_ID_URL = BOOK_COMMENT_DOMAIN_URL + "{commentId}";

    private static final String BOOK_ID = "1";
    private static final String BOOK_TITLE = "test";
    private static final String COMMENT_ID = "1";
    private static final String COMMENT_COMMENTATOR_NAME = "ivan";
    private static final String COMMENT_TEXT = "wow";

    private static WebTestClient testClient;
    private static ObjectMapper mapper;
    private static Comment comment;
    private static CommentDto commentDto;
    private static CommentListDto commentListDto;
    private static Book book;
    private static ErrorDto errorDtoNotFound;
    private static ErrorDto errorDtoNotFoundComment;

    @Configuration
    @Import(CommentFunctionalConfig.class)
    public static class CommentRouterConfig {
    }

    @Autowired
    @Qualifier("getCommentsByBookIdRoute")
    private RouterFunction<ServerResponse> getCommentsByBookIdRoute;

    @Autowired
    @Qualifier("addCommentRoute")
    private RouterFunction<ServerResponse> addCommentRoute;

    @Autowired
    @Qualifier("removeCommentRoute")
    private RouterFunction<ServerResponse> removeCommentRoute;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private MappingService mappingService;

    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
        book = Book.builder()
                .id(BOOK_ID)
                .title(BOOK_TITLE)
                .build();
        comment = Comment.builder()
                .id(COMMENT_ID)
                .commentatorName(COMMENT_COMMENTATOR_NAME)
                .text(COMMENT_TEXT)
                .book(book)
                .build();
        commentDto = CommentDto.builder()
                .id(COMMENT_ID)
                .commentatorName(COMMENT_COMMENTATOR_NAME)
                .text(COMMENT_TEXT)
                .build();
        commentListDto = CommentListDto.builder()
                .comments(List.of(commentDto))
                .build();
        errorDtoNotFound = ErrorDto.builder()
                .code(BOOK_NOT_FOUND.getCode())
                .message(BOOK_NOT_FOUND.getMessage())
                .build();
        errorDtoNotFoundComment = ErrorDto.builder()
                .code(COMMENT_NOT_FOUND.getCode())
                .message(COMMENT_NOT_FOUND.getMessage())
                .build();
    }

    @Test
    @DisplayName("return all book's comments")
    public void shouldReturnAllBookComments() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(getCommentsByBookIdRoute).build();

        when(commentRepository.findAllByBook_Id(BOOK_ID)).thenReturn(Flux.just(comment));
        when(mappingService.mapAsList(List.of(comment), CommentDto.class)).thenReturn(List.of(commentDto));

        testClient.get()
                .uri(uriBuilder -> uriBuilder.path(BOOK_COMMENT_DOMAIN_URL).build(BOOK_ID))
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(commentListDto));
    }

    @Test
    @DisplayName("add comment to book")
    public void shouldAddCommentToBook() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(addCommentRoute).build();

        when(bookRepository.findById(BOOK_ID)).thenReturn(Mono.just(book));
        when(commentRepository.save(Comment.builder()
                .commentatorName(commentDto.getCommentatorName())
                .text(commentDto.getText())
                .book(book)
                .build())).thenReturn(Mono.just(comment));
        when(mappingService.map(comment, CommentDto.class)).thenReturn(commentDto);

        testClient.post()
                .uri(uriBuilder -> uriBuilder.path(BOOK_COMMENT_DOMAIN_URL).build(BOOK_ID))
                .bodyValue(commentDto)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(commentDto));
    }

    @Test
    @DisplayName("not add comment to book because book doesn't exist")
    public void shouldNotAddCommentToBook() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(addCommentRoute).build();

        when(bookRepository.findById(BOOK_ID)).thenReturn(Mono.empty());

        testClient.post()
                .uri(uriBuilder -> uriBuilder.path(BOOK_COMMENT_DOMAIN_URL).build(BOOK_ID))
                .bodyValue(commentDto)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(errorDtoNotFound));
    }

    @Test
    @DisplayName("remove comment from book")
    public void shouldRemoveCommentFromBook() {
        testClient = WebTestClient.bindToRouterFunction(removeCommentRoute).build();

        when(bookRepository.existsById(BOOK_ID)).thenReturn(Mono.just(true));
        when(commentRepository.existsById(BOOK_ID)).thenReturn(Mono.just(true));
        when(commentRepository.deleteById(COMMENT_ID)).thenReturn(Mono.empty());

        testClient.delete()
                .uri(uriBuilder -> uriBuilder.path(BOOK_COMMENT_BY_ID_URL).build(Map.of("id", BOOK_ID, "commentId", COMMENT_ID)))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @DisplayName("not remove comment from book because book doesn't exist")
    public void shouldNotRemoveCommentFromBookBookError() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(removeCommentRoute).build();

        when(bookRepository.existsById(BOOK_ID)).thenReturn(Mono.just(false));

        testClient.delete()
                .uri(uriBuilder -> uriBuilder.path(BOOK_COMMENT_BY_ID_URL).build(Map.of("id", BOOK_ID, "commentId", COMMENT_ID)))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .json(mapper.writeValueAsString(errorDtoNotFound));
    }

    @Test
    @DisplayName("not remove comment from book because comment doesn't exist")
    public void shouldNotRemoveCommentFromBookCommentError() throws JsonProcessingException {
        testClient = WebTestClient.bindToRouterFunction(removeCommentRoute).build();

        when(bookRepository.existsById(BOOK_ID)).thenReturn(Mono.just(true));
        when(commentRepository.existsById(COMMENT_ID)).thenReturn(Mono.just(false));

        testClient.delete()
                .uri(uriBuilder -> uriBuilder.path(BOOK_COMMENT_BY_ID_URL).build(Map.of("id", BOOK_ID, "commentId", COMMENT_ID)))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .json(mapper.writeValueAsString(errorDtoNotFoundComment));
    }

}
