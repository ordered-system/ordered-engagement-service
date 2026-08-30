package pl.dybcio.ordered.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.dybcio.ordered.review.service.DuplicateReviewException;
import pl.dybcio.ordered.review.service.ProductNotPurchasedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
}
