package pl.dybcio.ordered.review.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OrderServiceClientTest {

  private MockRestServiceServer server;
  private OrderServiceClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    MockEnvironment env =
        new MockEnvironment().withProperty("app.order-service.base-url", "http://order-service");
    client = new OrderServiceClient(builder, env);
  }

  @Test
  void hasPurchased_returnsTrue_whenOrderServiceConfirmsPurchase() {
    server
        .expect(requestTo("http://order-service/internal/v1/orders/purchases/42/10"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"purchased\": true}", MediaType.APPLICATION_JSON));

    boolean result = client.hasPurchased(42L, 10L);

    assertThat(result).isTrue();
    server.verify();
  }

  @Test
  void hasPurchased_returnsFalse_whenOrderServiceDeniesPurchase() {
    server
        .expect(requestTo("http://order-service/internal/v1/orders/purchases/42/10"))
        .andRespond(withSuccess("{\"purchased\": false}", MediaType.APPLICATION_JSON));

    assertThat(client.hasPurchased(42L, 10L)).isFalse();
  }

  @Test
  void hasPurchased_throwsPurchaseVerificationException_on5xxResponse() {
    server
        .expect(requestTo("http://order-service/internal/v1/orders/purchases/42/10"))
        .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

    assertThatThrownBy(() -> client.hasPurchased(42L, 10L))
        .isInstanceOf(PurchaseVerificationException.class);
  }
}
