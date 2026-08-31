package pl.dybcio.ordered.history.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import pl.dybcio.ordered.commons.security.AuthenticatedUser;
import pl.dybcio.ordered.history.dto.BrowsingHistoryResponse;
import pl.dybcio.ordered.history.dto.RecordViewRequest;
import pl.dybcio.ordered.history.service.BrowsingHistoryService;

@RestController
@RequestMapping("/api/v1/browsing-history")
@RequiredArgsConstructor
@Tag(name = "Browsing history", description = "The authenticated user's own product view history")
public class BrowsingHistoryController {

  private final BrowsingHistoryService browsingHistoryService;

  @PostMapping
  @Operation(
      summary = "Record a product view",
      description =
          "In normal use this is called by product-service itself (fire-and-forget, on every"
              + " GET /api/v1/products/{id}), not directly by clients.")
  public ResponseEntity<Void> recordView(
      @Valid @RequestBody RecordViewRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    browsingHistoryService.recordView(user.userId(), request.productId());
    return ResponseEntity.accepted().build();
  }

  @GetMapping
  @Operation(summary = "List the authenticated user's own browsing history, most recent first")
  public List<BrowsingHistoryResponse> getMyHistory(
      @AuthenticationPrincipal AuthenticatedUser user, Pageable pageable) {
    return browsingHistoryService.getHistoryForUser(user.userId(), pageable).stream()
        .map(BrowsingHistoryResponse::from)
        .toList();
  }
}
