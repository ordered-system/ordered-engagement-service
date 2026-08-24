package pl.dybcio.ordered.history.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dybcio.ordered.history.dto.BrowsingHistoryResponse;
import pl.dybcio.ordered.history.dto.RecordViewRequest;
import pl.dybcio.ordered.history.service.BrowsingHistoryService;
import pl.dybcio.ordered.security.AuthenticatedUser;

@RestController
@RequestMapping("/api/v1/browsing-history")
@RequiredArgsConstructor
public class BrowsingHistoryController {

  private final BrowsingHistoryService browsingHistoryService;

  @PostMapping
  public ResponseEntity<Void> recordView(
      @Valid @RequestBody RecordViewRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    browsingHistoryService.recordView(user.userId(), request.productId());
    return ResponseEntity.accepted().build();
  }

  @GetMapping
  public List<BrowsingHistoryResponse> getMyHistory(
      @AuthenticationPrincipal AuthenticatedUser user, Pageable pageable) {
    return browsingHistoryService.getHistoryForUser(user.userId(), pageable).stream()
        .map(BrowsingHistoryResponse::from)
        .toList();
  }
}
