package pl.dybcio.ordered.purchase.dto;

import java.time.Instant;
import java.util.List;

public record OrderDeliveredPayload(
    Long orderId, Long buyerId, List<Long> productIds, Instant deliveredAt) {}
