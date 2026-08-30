package pl.dybcio.ordered.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dybcio.ordered.purchase.repository.VerifiedPurchaseRepository;
import pl.dybcio.ordered.review.dto.ReviewRequest;
import pl.dybcio.ordered.review.entity.Review;
import pl.dybcio.ordered.review.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @Mock private ReviewRepository reviewRepository;
  @Mock private VerifiedPurchaseRepository verifiedPurchaseRepository;

  private ReviewService reviewService;

  private ReviewService service() {
    if (reviewService == null) {
      reviewService = new ReviewService(reviewRepository, verifiedPurchaseRepository);
    }
    return reviewService;
  }

  @Test
  void addReview_throwsProductNotPurchased_whenNoVerifiedPurchaseOnRecord() {
    when(verifiedPurchaseRepository.existsByUserIdAndProductId(42L, 10L)).thenReturn(false);

    ReviewRequest request = new ReviewRequest(10L, 5, "Great!");

    assertThatThrownBy(() -> service().addReview(42L, "adam@example.com", request))
        .isInstanceOf(ProductNotPurchasedException.class);

    verifyNoInteractions(reviewRepository);
  }

  @Test
  void addReview_throwsDuplicateReview_whenAlreadyReviewed() {
    when(verifiedPurchaseRepository.existsByUserIdAndProductId(42L, 10L)).thenReturn(true);
    when(reviewRepository.existsByUserIdAndProductId(42L, 10L)).thenReturn(true);

    ReviewRequest request = new ReviewRequest(10L, 5, "Great!");

    assertThatThrownBy(() -> service().addReview(42L, "adam@example.com", request))
        .isInstanceOf(DuplicateReviewException.class);

    verify(reviewRepository, never()).save(any());
  }

  @Test
  void addReview_savesReview_whenPurchaseVerifiedAndNotYetReviewed() {
    when(verifiedPurchaseRepository.existsByUserIdAndProductId(42L, 10L)).thenReturn(true);
    when(reviewRepository.existsByUserIdAndProductId(42L, 10L)).thenReturn(false);
    when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

    ReviewRequest request = new ReviewRequest(10L, 5, "Great!");
    Review result = service().addReview(42L, "adam@example.com", request);

    assertThat(result.getProductId()).isEqualTo(10L);
    assertThat(result.getUserId()).isEqualTo(42L);
    assertThat(result.getUserEmail()).isEqualTo("adam@example.com");
    assertThat(result.getRating()).isEqualTo(5);
    assertThat(result.getCreatedAt()).isNotNull();
  }
}
