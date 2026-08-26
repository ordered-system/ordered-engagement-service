package pl.dybcio.ordered.history.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dybcio.ordered.history.dto.InternalRecordViewRequest;
import pl.dybcio.ordered.history.service.BrowsingHistoryService;

@RestController
@RequestMapping("/internal/v1/browsing-history")
@RequiredArgsConstructor
public class InternalBrowsingHistoryController {

  private final BrowsingHistoryService browsingHistoryService;

  @PostMapping
  public ResponseEntity<Void> recordView(@RequestBody InternalRecordViewRequest request) {
    browsingHistoryService.recordView(request.userId(), request.productId());
    return ResponseEntity.accepted().build();
  }
}
