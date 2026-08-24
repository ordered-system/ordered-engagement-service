package pl.dybcio.ordered.review.client;

public class PurchaseVerificationException extends RuntimeException {
  public PurchaseVerificationException(Long buyerId, String message) {
    super("Could not verify purchase for buyer " + buyerId + ": " + message);
  }
}
