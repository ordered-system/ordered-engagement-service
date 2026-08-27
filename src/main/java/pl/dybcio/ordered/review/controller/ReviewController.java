package pl.dybcio.ordered.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dybcio.ordered.commons.dto.PageResponse;
import pl.dybcio.ordered.commons.security.AuthenticatedUser;
import pl.dybcio.ordered.review.dto.ReviewRequest;
import pl.dybcio.ordered.review.dto.ReviewResponse;
import pl.dybcio.ordered.review.entity.Review;
import pl.dybcio.ordered.review.service.ReviewService;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<ReviewResponse> addReview(
      @Valid @RequestBody ReviewRequest request, @AuthenticationPrincipal AuthenticatedUser user) {
    Review review = reviewService.addReview(user.userId(), user.email(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponse.from(review));
  }

  @GetMapping("/product/{productId}")
  public PageResponse<ReviewResponse> getReviewsForProduct(
      @PathVariable Long productId, Pageable pageable) {
    return PageResponse.from(
        reviewService.getReviewsForProduct(productId, pageable).map(ReviewResponse::from));
  }
}
