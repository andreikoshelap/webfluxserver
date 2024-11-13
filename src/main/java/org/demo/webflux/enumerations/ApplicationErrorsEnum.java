package org.demo.webflux.enumerations;

public enum ApplicationErrorsEnum {

    GENRE_NOT_FOUND(1L, "Genre by id has not found"),
    AUTHOR_NOT_FOUND(2L, "Author by id has not found"),
    BOOK_NOT_FOUND(3L, "Book by id has not found"),
    COMMENT_NOT_FOUND(4L, "Comment by id has not found"),
    GENRE_RELATED_ENTITY(5L, "Genre has related books"),
    AUTHOR_RELATED_ENTITY(6L, "Author has related books");

    private final Long code;
    private final String message;

    ApplicationErrorsEnum(Long code, String message) {
        this.code = code;
        this.message = message;
    }

    public Long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
