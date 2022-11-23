package com.enmivida.review.exception;

public class ReviewDataException extends RuntimeException {
    private String message;

    public ReviewDataException(String message) {
        super(message);
        this.message = message;
    }
}
