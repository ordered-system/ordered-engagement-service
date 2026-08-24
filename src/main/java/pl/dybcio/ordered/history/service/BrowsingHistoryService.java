package pl.dybcio.ordered.history.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.dybcio.ordered.history.entity.BrowsingHistoryEntry;
import pl.dybcio.ordered.history.repository.BrowsingHistoryRepository;

@Service
@RequiredArgsConstructor
public class BrowsingHistoryService {

  private final BrowsingHistoryRepository browsingHistoryRepository;

  @Async
  public void recordView(Long userId, Long productId) {
    BrowsingHistoryEntry entry =
        BrowsingHistoryEntry.builder()
            .userId(userId)
            .productId(productId)
            .viewedAt(Instant.now())
            .build();
    browsingHistoryRepository.save(entry);
  }

  public List<BrowsingHistoryEntry> getHistoryForUser(Long userId, Pageable pageable) {
    return browsingHistoryRepository.findByUserIdOrderByViewedAtDesc(userId, pageable);
  }
}
