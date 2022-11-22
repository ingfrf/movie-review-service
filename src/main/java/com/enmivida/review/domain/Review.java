package com.enmivida.review.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document
public class Review {
    @Id
    private String reviewId;
    private Long movieInfoId;
    private String comment;
    private Double rating;
}
