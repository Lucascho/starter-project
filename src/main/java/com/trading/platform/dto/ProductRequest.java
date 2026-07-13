package com.trading.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
        @NotBlank String name,
        @PositiveOrZero double price,
        @NotNull @PositiveOrZero Integer stock
) {
}
