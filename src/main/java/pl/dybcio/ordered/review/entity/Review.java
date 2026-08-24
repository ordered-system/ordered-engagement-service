package pl.dybcio.ordered.review.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reviews")
@CompoundIndexes({
  @CompoundIndex(name = "product_created_idx", def = "{'productId': 1, 'createdAt': -1}"),
  @CompoundIndex(
      name = "user_product_unique_idx",
      def = "{'userId': 1, 'productId': 1}",
      unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

  @Id private String id;

  private Long productId;
  private Long userId;
  private String userEmail;

  private Integer rating;
  private String comment;

  private Instant createdAt;
}
