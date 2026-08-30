package pl.dybcio.ordered.purchase.entity;

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

@Document(collection = "verified_purchases")
@CompoundIndexes({
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
public class VerifiedPurchase {

  @Id private String id;

  private Long userId;
  private Long productId;
  private Long orderId;
  private Instant deliveredAt;
}
