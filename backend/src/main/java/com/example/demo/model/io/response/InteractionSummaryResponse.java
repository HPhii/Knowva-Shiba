package com.example.demo.model.io.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InteractionSummaryResponse {
    private Double averageRating;
    private Long totalRatings;
    private Long totalComments;
    private RatingDistribution ratingDistribution;

    @Data
    @Builder
    public static class RatingDistribution {
        private Long oneStar;
        private Long twoStar;
        private Long threeStar;
        private Long fourStar;
        private Long fiveStar;
    }
}
