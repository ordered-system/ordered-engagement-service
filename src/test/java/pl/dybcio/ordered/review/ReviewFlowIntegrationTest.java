package pl.dybcio.ordered.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.dybcio.ordered.purchase.entity.VerifiedPurchase;
import pl.dybcio.ordered.purchase.repository.VerifiedPurchaseRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestRestTemplate
class ReviewFlowIntegrationTest {

  private static final String JWT_SECRET = "test-only-signing-secret-not-used-for-any-real-auth";

  @Container @ServiceConnection
  static final MongoDBContainer mongo = new MongoDBContainer("mongo:7");

  @DynamicPropertySource
  static void extraProperties(DynamicPropertyRegistry registry) {
    registry.add("eureka.client.enabled", () -> "false");
    registry.add("app.jwt.secret", () -> JWT_SECRET);
  }

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private VerifiedPurchaseRepository verifiedPurchaseRepository;

  private String jwtFor(long userId, String email) {
    SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    Date now = new Date();
    return Jwts.builder()
        .subject(email)
        .claim("userId", userId)
        .claim("roles", List.of("ROLE_USER"))
        .issuedAt(now)
        .expiration(new Date(now.getTime() + 300_000))
        .signWith(key)
        .compact();
  }

  private HttpEntity<String> withAuth(String json, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);
    return new HttpEntity<>(json, headers);
  }

  @Test
  void addReview_thenSecondAttemptOnSameProduct_returns409() {
    String token = jwtFor(42L, "adam@example.com");
    verifiedPurchaseRepository.save(
        VerifiedPurchase.builder().userId(42L).productId(10L).orderId(1L).build());

    ResponseEntity<String> first =
        restTemplate.postForEntity(
            "/api/v1/reviews",
            withAuth(
                """
                {"productId":10,"rating":5,"comment":"Great!"}
                """,
                token),
            String.class);
    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    ResponseEntity<String> second =
        restTemplate.postForEntity(
            "/api/v1/reviews",
            withAuth(
                """
                {"productId":10,"rating":4,"comment":"Second try"}
                """,
                token),
            String.class);
    assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void addReview_returns403_whenProductNotPurchased() {
    String token = jwtFor(43L, "notabuyer@example.com");

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/api/v1/reviews",
            withAuth(
                """
                {"productId":20,"rating":5,"comment":"Never bought this"}
                """,
                token),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void getReviewsForProduct_isPublic_noTokenNeeded() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/api/v1/reviews/product/999", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void browsingHistory_requiresAuth_andRecordsAsynchronously() {
    String token = jwtFor(44L, "browser@example.com");

    ResponseEntity<String> unauth =
        restTemplate.getForEntity("/api/v1/browsing-history", String.class);
    assertThat(unauth.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);
    ResponseEntity<String> recordResponse =
        restTemplate.postForEntity(
            "/api/v1/browsing-history",
            new HttpEntity<>(
                """
                {"productId":77}
                """,
                headers),
            String.class);
    assertThat(recordResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              ResponseEntity<Map[]> historyResponse =
                  restTemplate.exchange(
                      "/api/v1/browsing-history",
                      HttpMethod.GET,
                      new HttpEntity<>(headers),
                      Map[].class);
              assertThat(historyResponse.getBody()).isNotEmpty();
              assertThat(historyResponse.getBody()[0].get("productId")).isEqualTo(77);
            });
  }
}
