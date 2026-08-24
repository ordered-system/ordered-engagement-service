package pl.dybcio.ordered.review.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class OrderServiceClient {

  private final RestClient restClient;

  public OrderServiceClient(RestClient.Builder builder, Environment environment) {
    String baseUrl = environment.getProperty("app.order-service.base-url");
    this.restClient = builder.baseUrl(baseUrl).build();
  }

  @CircuitBreaker(name = "orderService", fallbackMethod = "hasPurchasedFallback")
  @Retry(name = "orderService", fallbackMethod = "hasPurchasedFallback")
  public boolean hasPurchased(Long buyerId, Long productId) {
    try {
      PurchaseCheckResponse response =
          restClient
              .get()
              .uri("/internal/v1/orders/purchases/{buyerId}/{productId}", buyerId, productId)
              .retrieve()
              .body(PurchaseCheckResponse.class);
      return response != null && response.purchased();
    } catch (RestClientResponseException e) {
      throw new PurchaseVerificationException(buyerId, e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  private boolean hasPurchasedFallback(Long buyerId, Long productId, Throwable throwable) {
    throw new PurchaseVerificationException(
        buyerId, "order-service is currently unavailable, please try again shortly");
  }

  private record PurchaseCheckResponse(boolean purchased) {}
}
