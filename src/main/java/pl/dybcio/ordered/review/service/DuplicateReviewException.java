package pl.dybcio.ordered.review.service;

public class DuplicateReviewException extends RuntimeException {
  public DuplicateReviewException(Long productId) {
    super("User has already reviewed product " + productId);
  }
}
