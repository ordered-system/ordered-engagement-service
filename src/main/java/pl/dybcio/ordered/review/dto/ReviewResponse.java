package pl.dybcio.ordered.review.dto;

import java.time.Instant;
import pl.dybcio.ordered.review.entity.Review;

public record ReviewResponse(
    String id,
    Long productId,
    Long userId,
    String userEmail,
    Integer rating,
    String comment,
    Instant createdAt) {

  public static ReviewResponse from(Review review) {
    return new ReviewResponse(
        review.getId(),
        review.getProductId(),
        review.getUserId(),
        review.getUserEmail(),
        review.getRating(),
        review.getComment(),
        review.getCreatedAt());
  }
}
