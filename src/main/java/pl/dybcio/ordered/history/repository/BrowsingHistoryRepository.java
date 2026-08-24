package pl.dybcio.ordered.history.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import pl.dybcio.ordered.history.entity.BrowsingHistoryEntry;

public interface BrowsingHistoryRepository extends MongoRepository<BrowsingHistoryEntry, String> {

  List<BrowsingHistoryEntry> findByUserIdOrderByViewedAtDesc(Long userId, Pageable pageable);
}
