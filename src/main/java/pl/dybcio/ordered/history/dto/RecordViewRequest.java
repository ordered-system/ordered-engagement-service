package pl.dybcio.ordered.history.dto;

import jakarta.validation.constraints.NotNull;

public record RecordViewRequest(@NotNull Long productId) {}
