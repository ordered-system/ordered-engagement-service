package pl.dybcio.ordered.purchase.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.dybcio.ordered.purchase.entity.VerifiedPurchase;

public interface VerifiedPurchaseRepository extends MongoRepository<VerifiedPurchase, String> {

  boolean existsByUserIdAndProductId(Long userId, Long productId);
}
