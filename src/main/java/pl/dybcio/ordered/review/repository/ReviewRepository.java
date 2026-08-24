package pl.dybcio.ordered.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import pl.dybcio.ordered.review.entity.Review;

public interface ReviewRepository extends MongoRepository<Review, String> {

  Page<Review> findByProductId(Long productId, Pageable pageable);

  boolean existsByUserIdAndProductId(Long userId, Long productId);
}
