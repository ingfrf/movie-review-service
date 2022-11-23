package com.enmivida.review.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document
public class Review {
    @Id
    private String reviewId;
    @NotNull(message = "review.movieInfoID: must not be null")
    private Long movieInfoId;
    private String comment;
    @Min(value = 0L, message = "review.rating: please pass a non-negative value")
    private Double rating;
}
