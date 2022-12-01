package com.enmivida.review.handler;

import com.enmivida.review.domain.Review;
import com.enmivida.review.exception.ReviewDataException;
import com.enmivida.review.repository.ReviewReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewHandler {
    private final ReviewReactiveRepository repository;
    private final Validator validator;
    private Sinks.Many<Review> reviewSink = Sinks.many().replay().latest();

    public Mono<ServerResponse> hello(ServerRequest request) {
        return ServerResponse.ok().bodyValue("helloworld2");
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                //.flatMap(review -> repository.save(review))
                .flatMap(repository::save)
                .doOnNext(review -> reviewSink.tryEmitNext(review))
                //.flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview))
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue)
                ;
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        Optional<String> idOptional = request.queryParam("movieInfoId");
        Flux<Review> reviewsFlux;
        if (idOptional.isPresent()) {
            reviewsFlux = repository.findReviewsByMovieInfoId(Long.valueOf(idOptional.get()));
        } else {
            reviewsFlux = repository.findAll();
        }
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        String reviewId = request.pathVariable("id");
        Mono<Review> existingReview = repository.findById(reviewId)
               // .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id "+reviewId)))
                ;

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(requestReview -> {
                            review.setComment(requestReview.getComment());
                            review.setRating(requestReview.getRating());
                            return review;
                        })
                        .flatMap(repository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                        .switchIfEmpty(ServerResponse.notFound().build())
                )
                ;
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        String reviewId = request.pathVariable("id");
        Mono<Review> existingReview = repository.findById(reviewId);
        return existingReview
                .flatMap(review -> repository.deleteById(reviewId))
                // retorna un Mono<Void> por lo que se crea una custom response
                .then(ServerResponse.noContent().build())
                ;
    }

    private void validate(Review review) {
        Set<ConstraintViolation<Review>> constraintViolations = validator.validate(review);
        log.error("constraintViolations : {}", constraintViolations);
        if (constraintViolations.size() > 0) {
            String errorMessage = constraintViolations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewSink.asFlux(), Review.class)
                .log()
                ;
    }
}
