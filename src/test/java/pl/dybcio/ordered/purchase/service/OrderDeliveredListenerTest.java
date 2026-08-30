package pl.dybcio.ordered.purchase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.Instant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dybcio.ordered.messaging.entity.ProcessedEvent;
import pl.dybcio.ordered.messaging.repository.ProcessedEventRepository;
import pl.dybcio.ordered.purchase.entity.VerifiedPurchase;
import pl.dybcio.ordered.purchase.repository.VerifiedPurchaseRepository;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OrderDeliveredListenerTest {

  @Mock private ProcessedEventRepository processedEventRepository;
  @Mock private VerifiedPurchaseRepository verifiedPurchaseRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private OrderDeliveredListener listener;

  private OrderDeliveredListener listener() {
    if (listener == null) {
      listener =
          new OrderDeliveredListener(
              processedEventRepository, verifiedPurchaseRepository, objectMapper);
    }
    return listener;
  }

  private ConsumerRecord<String, String> record(
      String eventId, long orderId, long buyerId, long... productIds) {
    String ids =
        java.util.stream.LongStream.of(productIds)
            .mapToObj(String::valueOf)
            .collect(java.util.stream.Collectors.joining(","));
    String payload =
        """
        {"orderId": %d, "buyerId": %d, "productIds": [%s], "deliveredAt": "%s"}
        """
            .formatted(orderId, buyerId, ids, Instant.now());
    return new ConsumerRecord<>("order-delivered", 0, 0L, eventId, payload);
  }

  @Test
  void onOrderDelivered_savesOneVerifiedPurchasePerProduct_andRecordsProcessedEvent() {
    when(processedEventRepository.existsById("evt-1")).thenReturn(false);
    when(verifiedPurchaseRepository.existsByUserIdAndProductId(anyLong(), anyLong()))
        .thenReturn(false);

    listener().onOrderDelivered(record("evt-1", 100L, 42L, 10L, 20L));

    ArgumentCaptor<VerifiedPurchase> captor = ArgumentCaptor.forClass(VerifiedPurchase.class);
    verify(verifiedPurchaseRepository, times(2)).save(captor.capture());
    assertThat(captor.getAllValues())
        .extracting(VerifiedPurchase::getProductId)
        .containsExactlyInAnyOrder(10L, 20L);
    assertThat(captor.getAllValues())
        .allSatisfy(
            vp -> {
              assertThat(vp.getUserId()).isEqualTo(42L);
              assertThat(vp.getOrderId()).isEqualTo(100L);
            });

    verify(processedEventRepository)
        .save(
            argThat(
                (ProcessedEvent e) ->
                    e.getId().equals("evt-1") && e.getEventType().equals("OrderDelivered")));
  }

  @Test
  void onOrderDelivered_skipsAlreadyVerifiedProduct_butStillMarksEventProcessed() {
    when(processedEventRepository.existsById("evt-2")).thenReturn(false);
    when(verifiedPurchaseRepository.existsByUserIdAndProductId(42L, 10L)).thenReturn(true);

    listener().onOrderDelivered(record("evt-2", 100L, 42L, 10L));

    verify(verifiedPurchaseRepository, never()).save(any());
    verify(processedEventRepository).save(any(ProcessedEvent.class));
  }

  @Test
  void onOrderDelivered_skipsEverything_whenEventAlreadyProcessed() {
    when(processedEventRepository.existsById("evt-3")).thenReturn(true);

    listener().onOrderDelivered(record("evt-3", 100L, 42L, 10L));

    verifyNoInteractions(verifiedPurchaseRepository);
    verify(processedEventRepository, never()).save(any(ProcessedEvent.class));
  }
}
