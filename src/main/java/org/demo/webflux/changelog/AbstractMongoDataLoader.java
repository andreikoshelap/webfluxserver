package org.demo.webflux.changelog;

import lombok.val;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.demo.webflux.domain.Author;
import org.demo.webflux.domain.Book;
import org.demo.webflux.domain.Comment;
import org.demo.webflux.domain.Genre;

import java.util.Arrays;

public class AbstractMongoDataLoader {

    private final ReactiveMongoTemplate mongoTemplate;

    public AbstractMongoDataLoader(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    protected void cleanData() {
        mongoTemplate.remove(new Query(), Book.class).subscribe(result -> {});
        mongoTemplate.remove(new Query(), Comment.class).subscribe(result -> {});
        mongoTemplate.remove(new Query(), Genre.class).subscribe(result -> {});
        mongoTemplate.remove(new Query(), Author.class).subscribe(result -> {});
    }

    protected void loadData() {
        val genre1 = Genre.builder()
                .id("1")
                .name("Драма")
                .build();

        val genre2 = Genre.builder()
                .id("2")
                .name("Фантастика")
                .build();

        val genre3 = Genre.builder()
                .id("3")
                .name("Антиутопия")
                .build();

        val genre4 = Genre.builder()
                .id("4")
                .name("Роман")
                .build();

        val genre5 = Genre.builder()
                .id("5")
                .name("Детектив")
                .build();

        val genre6 = Genre.builder()
                .id("6")
                .name("Триллер")
                .build();

        val genre7 = Genre.builder()
                .id("7")
                .name("Ужасы")
                .build();

        val genre8 = Genre.builder()
                .id("8")
                .name("Комедия")
                .build();

        val genre9 = Genre.builder()
                .id("9")
                .name("Фэнтези")
                .build();

        val genre10 = Genre.builder()
                .id("10")
                .name("Научпоп")
                .build();

        mongoTemplate.insertAll(Arrays.asList(genre1, genre2, genre3, genre4, genre5,
                genre6, genre7, genre8, genre9, genre10)).subscribe(result -> {
        });

        val author = Author.builder()
                .id("1")
                .firstName("Сергей")
                .lastName("Лукьяненко")
                .build();

        val author2 = Author.builder()
                .id("2")
                .firstName("Джордж")
                .lastName("Оруэлл")
                .build();

        val author3 = Author.builder()
                .id("3")
                .firstName("Рэй")
                .lastName("Бредбери")
                .build();

        val author4 = Author.builder()
                .id("4")
                .firstName("Борис")
                .lastName("Акунин")
                .build();

        val author5 = Author.builder()
                .id("5")
                .firstName("Агата")
                .lastName("Кристи")
                .build();

        val author6 = Author.builder()
                .id("6")
                .firstName("Федор")
                .lastName("Достоевский")
                .build();

        val author7 = Author.builder()
                .id("7")
                .firstName("Александр")
                .lastName("Пушкин")
                .build();

        val author8 = Author.builder()
                .id("8")
                .firstName("Лев")
                .lastName("Толстой")
                .build();

        val author9 = Author.builder()
                .id("9")
                .firstName("Джордж")
                .lastName("Мартин")
                .build();

        val author10 = Author.builder()
                .id("10")
                .firstName("Джон")
                .lastName("Толкиен")
                .build();

        mongoTemplate.insertAll(Arrays.asList(author, author2, author3, author4, author5,
                author6, author7, author8, author9, author10)).subscribe(result -> {
        });

        val book = Book.builder()
                .id("1")
                .title("Черновик")
                .genre(genre2)
                .author(author)
                .build();

        val book2 = Book.builder()
                .id("2")
                .title("Чистовик")
                .genre(genre2)
                .author(author)
                .build();

        val book3 = Book.builder()
                .id("3")
                .title("1984")
                .genre(genre3)
                .author(author2)
                .build();

        val book4 = Book.builder()
                .id("4")
                .title("Скотный двор")
                .genre(genre3)
                .author(author2)
                .build();

        val book5 = Book.builder()
                .id("5")
                .title("451 градус по фаренгейту")
                .genre(genre3)
                .author(author3)
                .build();

        val book6 = Book.builder()
                .id("6")
                .title("Убийство в восточном экспрессе")
                .genre(genre5)
                .author(author5)
                .build();

        val book7 = Book.builder()
                .id("7")
                .title("Братья Карамазовы")
                .genre(genre4)
                .author(author6)
                .build();

        val book8 = Book.builder()
                .id("8")
                .title("Турецкий гамбит")
                .genre(genre5)
                .author(author4)
                .build();

        val book9 = Book.builder()
                .id("9")
                .title("Властелин колец")
                .genre(genre9)
                .author(author10)
                .build();

        val book10 = Book.builder()
                .id("10")
                .title("Песнь льда и пламени: Ветра зимы")
                .genre(genre9)
                .author(author9)
                .build();

        mongoTemplate.insertAll(Arrays.asList(book, book2, book3, book4, book5,
                book6, book7, book8, book9, book10)).subscribe(result -> {
        });

        val comment = Comment.builder()
                .commentatorName("Вася Пупкин")
                .text("Книга бомба!")
                .book(book3)
                .build();

        val comment2 = Comment.builder()
                .commentatorName("Саня Петров")
                .text("Первая часть была лучше")
                .book(book2)
                .build();

        val comment3 = Comment.builder()
                .commentatorName("Володя Путин")
                .text("Лол, жиза! У меня в стране также")
                .book(book3)
                .build();

        mongoTemplate.insertAll(Arrays.asList(comment, comment2, comment3)).subscribe(result -> {
        });
    }
}
