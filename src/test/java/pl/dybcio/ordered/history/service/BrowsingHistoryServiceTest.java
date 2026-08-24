package pl.dybcio.ordered.history.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import pl.dybcio.ordered.history.entity.BrowsingHistoryEntry;
import pl.dybcio.ordered.history.repository.BrowsingHistoryRepository;

@ExtendWith(MockitoExtension.class)
class BrowsingHistoryServiceTest {

  @Mock private BrowsingHistoryRepository browsingHistoryRepository;
  private BrowsingHistoryService browsingHistoryService;

  private BrowsingHistoryService service() {
    if (browsingHistoryService == null) {
      browsingHistoryService = new BrowsingHistoryService(browsingHistoryRepository);
    }
    return browsingHistoryService;
  }

  @Test
  void recordView_savesEntryWithCurrentTimestamp() {
    service().recordView(42L, 10L);

    ArgumentCaptor<BrowsingHistoryEntry> captor =
        ArgumentCaptor.forClass(BrowsingHistoryEntry.class);
    verify(browsingHistoryRepository).save(captor.capture());
    assertThat(captor.getValue().getUserId()).isEqualTo(42L);
    assertThat(captor.getValue().getProductId()).isEqualTo(10L);
    assertThat(captor.getValue().getViewedAt()).isNotNull();
  }

  @Test
  void getHistoryForUser_delegatesToRepository() {
    Pageable pageable = Pageable.ofSize(20);
    BrowsingHistoryEntry entry =
        BrowsingHistoryEntry.builder().id("1").userId(42L).productId(10L).build();
    when(browsingHistoryRepository.findByUserIdOrderByViewedAtDesc(42L, pageable))
        .thenReturn(List.of(entry));

    List<BrowsingHistoryEntry> result = service().getHistoryForUser(42L, pageable);

    assertThat(result).containsExactly(entry);
  }
}
