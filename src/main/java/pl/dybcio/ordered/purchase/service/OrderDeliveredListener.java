package pl.dybcio.ordered.purchase.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.dybcio.ordered.messaging.KafkaTopics;
import pl.dybcio.ordered.messaging.entity.ProcessedEvent;
import pl.dybcio.ordered.messaging.repository.ProcessedEventRepository;
import pl.dybcio.ordered.purchase.dto.OrderDeliveredPayload;
import pl.dybcio.ordered.purchase.entity.VerifiedPurchase;
import pl.dybcio.ordered.purchase.repository.VerifiedPurchaseRepository;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderDeliveredListener {

  private final ProcessedEventRepository processedEventRepository;
  private final VerifiedPurchaseRepository verifiedPurchaseRepository;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = KafkaTopics.ORDER_DELIVERED,
      groupId = "${spring.kafka.consumer.group-id}")
  public void onOrderDelivered(ConsumerRecord<String, String> record) {
    String eventId = record.key();

    if (processedEventRepository.existsById(eventId)) {
      log.info("Event {} already processed, skipping (idempotency check)", eventId);
      return;
    }

    OrderDeliveredPayload payload =
        objectMapper.readValue(record.value(), OrderDeliveredPayload.class);

    log.info(
        "Processing OrderDelivered: orderId={}, buyerId={}, productIds={}",
        payload.orderId(),
        payload.buyerId(),
        payload.productIds());

    for (Long productId : payload.productIds()) {
      if (verifiedPurchaseRepository.existsByUserIdAndProductId(payload.buyerId(), productId)) {
        continue;
      }
      verifiedPurchaseRepository.save(
          VerifiedPurchase.builder()
              .userId(payload.buyerId())
              .productId(productId)
              .orderId(payload.orderId())
              .deliveredAt(payload.deliveredAt())
              .build());
    }

    processedEventRepository.save(
        ProcessedEvent.builder()
            .id(eventId)
            .eventType("OrderDelivered")
            .processedAt(Instant.now())
            .build());
  }
}
