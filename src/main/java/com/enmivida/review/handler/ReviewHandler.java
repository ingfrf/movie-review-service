package com.enmivida.review.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {

    public Mono<ServerResponse> hello(ServerRequest request) {
        return ServerResponse.ok().bodyValue("helloworld2");
    }
}
