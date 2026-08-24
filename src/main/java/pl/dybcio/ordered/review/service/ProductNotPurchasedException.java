package pl.dybcio.ordered.review.service;

public class ProductNotPurchasedException extends RuntimeException {
  public ProductNotPurchasedException(Long productId) {
    super("Cannot review product " + productId + ": no delivered order found for this user");
  }
}
