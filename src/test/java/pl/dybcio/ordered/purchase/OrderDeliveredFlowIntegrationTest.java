package pl.dybcio.ordered.purchase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import pl.dybcio.ordered.purchase.repository.VerifiedPurchaseRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderDeliveredFlowIntegrationTest {

  @Container @ServiceConnection
  static final MongoDBContainer mongo = new MongoDBContainer("mongo:7");

  @Container @ServiceConnection
  static final ConfluentKafkaContainer kafka =
      new ConfluentKafkaContainer("confluentinc/cp-kafka:7.7.0");

  @DynamicPropertySource
  static void extraProperties(DynamicPropertyRegistry registry) {
    registry.add("eureka.client.enabled", () -> "false");
    registry.add("app.jwt.secret", () -> "test-only-signing-secret-not-used-for-any-real-auth");
    registry.add(
        "spring.kafka.producer.key-serializer",
        () -> "org.apache.kafka.common.serialization.StringSerializer");
    registry.add(
        "spring.kafka.producer.value-serializer",
        () -> "org.apache.kafka.common.serialization.StringSerializer");
  }

  @Autowired private KafkaTemplate<String, String> kafkaTemplate;
  @Autowired private VerifiedPurchaseRepository verifiedPurchaseRepository;

  @Test
  void publishingOrderDelivered_eventuallyCreatesVerifiedPurchases_forEveryProductInTheOrder() {
    String payload =
        """
        {"orderId": 555, "buyerId": 88, "productIds": [10, 20, 30], "deliveredAt": "%s"}
        """
            .formatted(Instant.now());

    kafkaTemplate.send("order-delivered", "evt-integration-1", payload);

    await()
        .atMost(Duration.ofSeconds(15))
        .untilAsserted(
            () -> {
              assertThat(verifiedPurchaseRepository.existsByUserIdAndProductId(88L, 10L)).isTrue();
              assertThat(verifiedPurchaseRepository.existsByUserIdAndProductId(88L, 20L)).isTrue();
              assertThat(verifiedPurchaseRepository.existsByUserIdAndProductId(88L, 30L)).isTrue();
            });
  }

  @Test
  void publishingSameEventTwice_doesNotDuplicateVerifiedPurchases() throws InterruptedException {
    String payload =
        """
        {"orderId": 556, "buyerId": 89, "productIds": [40], "deliveredAt": "%s"}
        """
            .formatted(Instant.now());

    kafkaTemplate.send("order-delivered", "evt-integration-2", payload);
    kafkaTemplate.send("order-delivered", "evt-integration-2", payload);

    await()
        .atMost(Duration.ofSeconds(15))
        .untilAsserted(
            () ->
                assertThat(verifiedPurchaseRepository.existsByUserIdAndProductId(89L, 40L))
                    .isTrue());

    Thread.sleep(2000);
    long count =
        verifiedPurchaseRepository.findAll().stream()
            .filter(vp -> vp.getUserId().equals(89L) && vp.getProductId().equals(40L))
            .count();
    assertThat(count).isEqualTo(1);
  }
}
