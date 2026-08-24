package pl.dybcio.ordered.history.dto;

import java.time.Instant;
import pl.dybcio.ordered.history.entity.BrowsingHistoryEntry;

public record BrowsingHistoryResponse(String id, Long productId, Instant viewedAt) {

  public static BrowsingHistoryResponse from(BrowsingHistoryEntry entry) {
    return new BrowsingHistoryResponse(entry.getId(), entry.getProductId(), entry.getViewedAt());
  }
}
