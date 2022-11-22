package com.enmivida.review.handler;

import com.enmivida.review.domain.Review;
import com.enmivida.review.repository.ReviewReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewHandler {
    private final ReviewReactiveRepository repository;

    public Mono<ServerResponse> hello(ServerRequest request) {
        return ServerResponse.ok().bodyValue("helloworld2");
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                //.flatMap(review -> repository.save(review))
                .flatMap(repository::save)
                //.flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview))
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue)
                ;
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        Optional<String> idOptional = request.queryParam("movieInfoId");
        if (idOptional.isPresent()) {
            Flux<Review> reviewsFlux = repository.findReviewsByMovieInfoId(Long.valueOf(idOptional.get()));
            return ServerResponse.ok().body(reviewsFlux, Review.class);
        } else {
            Flux<Review> reviewsFlux = repository.findAll();
            return ServerResponse.ok().body(reviewsFlux, Review.class);
        }
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        String reviewId = request.pathVariable("id");
        Mono<Review> existingReview = repository.findById(reviewId);
        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(requestReview -> {
                            review.setComment(requestReview.getComment());
                            review.setRating(requestReview.getRating());
                            return review;
                        })
                        .flatMap(repository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
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
}
