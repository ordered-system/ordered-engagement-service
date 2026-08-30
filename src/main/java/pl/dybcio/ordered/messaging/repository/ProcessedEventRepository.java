package pl.dybcio.ordered.messaging.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.dybcio.ordered.messaging.entity.ProcessedEvent;

public interface ProcessedEventRepository extends MongoRepository<ProcessedEvent, String> {}
