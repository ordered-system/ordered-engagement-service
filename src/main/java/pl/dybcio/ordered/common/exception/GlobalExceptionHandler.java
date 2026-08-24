package pl.dybcio.ordered.common.exception;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.dybcio.ordered.review.client.PurchaseVerificationException;
import pl.dybcio.ordered.review.service.DuplicateReviewException;
import pl.dybcio.ordered.review.service.ProductNotPurchasedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(ProductNotPurchasedException.class)
  public ProblemDetail handleProductNotPurchased(ProductNotPurchasedException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    pd.setTitle("Product not purchased");
    return pd;
  }

  @ExceptionHandler(DuplicateReviewException.class)
  public ProblemDetail handleDuplicateReview(DuplicateReviewException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    pd.setTitle("Already reviewed");
    return pd;
  }

  @ExceptionHandler(PurchaseVerificationException.class)
  public ProblemDetail handlePurchaseVerificationFailure(PurchaseVerificationException ex) {
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    pd.setTitle("Could not verify purchase");
    return pd;
  }
}
