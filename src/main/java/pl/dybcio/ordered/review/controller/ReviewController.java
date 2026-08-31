package pl.dybcio.ordered.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Reviews", description = "Product reviews, gated on a verified delivered purchase")
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  @Operation(
      summary = "Add a review",
      description =
          "Requires a verified purchase - the buyer's order must have reached DELIVERED and been"
              + " picked up by the order-delivered Kafka consumer. Returns 403 otherwise, 409 if"
              + " the user already reviewed this product.")
  public ResponseEntity<ReviewResponse> addReview(
      @Valid @RequestBody ReviewRequest request, @AuthenticationPrincipal AuthenticatedUser user) {
    Review review = reviewService.addReview(user.userId(), user.email(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponse.from(review));
  }

  @GetMapping("/product/{productId}")
  @Operation(summary = "List reviews for a product, paginated (public, no token required)")
  @SecurityRequirements
  public PageResponse<ReviewResponse> getReviewsForProduct(
      @PathVariable Long productId, Pageable pageable) {
    return PageResponse.from(
        reviewService.getReviewsForProduct(productId, pageable).map(ReviewResponse::from));
  }
}
