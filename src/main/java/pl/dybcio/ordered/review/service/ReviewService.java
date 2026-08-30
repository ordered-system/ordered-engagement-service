package pl.dybcio.ordered.review.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.dybcio.ordered.purchase.repository.VerifiedPurchaseRepository;
import pl.dybcio.ordered.review.dto.ReviewRequest;
import pl.dybcio.ordered.review.entity.Review;
import pl.dybcio.ordered.review.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final VerifiedPurchaseRepository verifiedPurchaseRepository;

  public Review addReview(Long userId, String userEmail, ReviewRequest request) {
    boolean purchased =
        verifiedPurchaseRepository.existsByUserIdAndProductId(userId, request.productId());
    if (!purchased) {
      throw new ProductNotPurchasedException(request.productId());
    }

    if (reviewRepository.existsByUserIdAndProductId(userId, request.productId())) {
      throw new DuplicateReviewException(request.productId());
    }

    Review review =
        Review.builder()
            .productId(request.productId())
            .userId(userId)
            .userEmail(userEmail)
            .rating(request.rating())
            .comment(request.comment())
            .createdAt(Instant.now())
            .build();

    return reviewRepository.save(review);
  }

  public Page<Review> getReviewsForProduct(Long productId, Pageable pageable) {
    return reviewRepository.findByProductId(productId, pageable);
  }
}
