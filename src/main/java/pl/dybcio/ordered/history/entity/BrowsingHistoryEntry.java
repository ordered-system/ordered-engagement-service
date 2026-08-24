package pl.dybcio.ordered.history.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "browsing_history")
@CompoundIndexes({@CompoundIndex(name = "user_viewed_idx", def = "{'userId': 1, 'viewedAt': -1}")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrowsingHistoryEntry {

  @Id private String id;

  private Long userId;
  private Long productId;

  @Indexed(name = "viewed_at_ttl_idx", expireAfter = "90d")
  private Instant viewedAt;
}
