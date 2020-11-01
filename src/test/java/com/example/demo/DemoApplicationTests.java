package com.example.demo;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Notification;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.Assert.*;

//@SpringBootTest(classes = DemoApplication.class)
@RunWith(JUnit4.class)
class DemoApplicationTests {

    String api = "http://localhost:8080/api";

    ParameterizedTypeReference<PagedResult<Comment>> commentPageReference = new ParameterizedTypeReference<PagedResult<Comment>>() {
    };

    ParameterizedTypeReference<PagedResult<Notification>> notificationPageReference = new ParameterizedTypeReference<PagedResult<Notification>>() {
    };

    RestTemplate rest;

    @BeforeEach
    public void setUp() {
        rest = new RestTemplate();
        ResponseEntity<String> response = rest.exchange(api + "/clean", HttpMethod.GET, null, String.class);

    }

    @Test
    void createComments_Test() throws IOException, ExecutionException, InterruptedException {

        long commentsCount = 1000;

        long startTime = System.nanoTime();
        List<Comment> comments = createCommentsParallel(commentsCount);
        long endTime = System.nanoTime();

        long totalTimeNano = endTime - startTime;

        long totalTimeSec = totalTimeNano / 1_000_000_000;

        assertNotNull(comments);


        ResponseEntity<? extends PagedResult<Comment>> commentPage = rest.exchange(api + "/comments?page=0&size=" + commentsCount, HttpMethod.GET, null, commentPageReference);


        ResponseEntity<? extends PagedResult<Notification>> notificationPage = rest.exchange(api + "/notifications?page=0&size=" + commentsCount, HttpMethod.GET, null, notificationPageReference);

        int commentsSize = comments.size();

        int commentsPageSize = commentPage.getBody().getContent().size();

        int notificationsPageSize = notificationPage.getBody().getContent().size();

        assertEquals(commentPage.getStatusCode(), HttpStatus.OK);
        assertNotNull(commentPage.getBody().getContent());
        assertEquals(commentsPageSize, commentsSize);


        assertEquals(notificationPage.getStatusCode(), HttpStatus.OK);
        assertNotNull(notificationPage.getBody().getContent());
        assertEquals(notificationsPageSize, commentsSize);

        assertEquals(notificationsPageSize, commentsPageSize);

        comments.forEach(c -> {
            assertTrue(notificationPage.getBody().getContent().stream().anyMatch(n -> n.getComment().getId().equals(c.getId())));

            assertTrue(commentPage.getBody().getContent().stream().anyMatch(n -> n.getId().equals(c.getId())));
        });


        long createdCommentsPercent = commentsPageSize * 100 / commentsCount;

        long createdNotifiesPercent = notificationsPageSize * 100 / commentsCount;

        System.out.println("Comments creations seconds: " + totalTimeSec + "s");
        System.out.println("Comments success creation percent: " + createdCommentsPercent + "%");
        System.out.println("Notifications success creation percent: " + createdNotifiesPercent + "%");

    }

    List<Comment> createCommentsParallel(long commentCount) throws ExecutionException, InterruptedException {
        List<Long> list = LongStream.rangeClosed(1, commentCount).boxed().collect(Collectors.toList());
        int cores = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(cores);

        ForkJoinTask<List<Comment>> fork = pool.submit(() -> list.parallelStream().map((v) -> createComment(v.toString())).filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return fork.get();
    }

    Comment createComment(String text) {
        ResponseEntity<Comment> commentResponse = null;

        try {
            commentResponse = rest.exchange(api + "/comments", HttpMethod.POST, new HttpEntity<String>(text), Comment.class);
        } catch (RestClientException ex) {
            return null;
        }

        return commentResponse.getBody();

    }


}


class PagedResult<T> extends PageImpl<T> {

    boolean last;
    boolean first;
    int totalPages;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PagedResult(@JsonProperty("content") List<T> content, @JsonProperty("number") int number,
                       @JsonProperty("size") int size, @JsonProperty("totalElements") Long totalElements,
                       @JsonProperty("pageable") JsonNode pageable, @JsonProperty("last") boolean last,
                       @JsonProperty("totalPages") int totalPages,
                       @JsonProperty("sort") JsonNode sort,
                       @JsonProperty("first") boolean first, @JsonProperty("numberOfElements") int numberOfElements) {
        super(content, PageRequest.of(number, size), totalElements);
    }

    public PagedResult(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public PagedResult(List<T> content) {
        super(content);
    }

    public PagedResult() {
        super(new ArrayList<T>());
    }

}

