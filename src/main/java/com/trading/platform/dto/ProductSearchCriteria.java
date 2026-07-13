package com.trading.platform.dto;

public record ProductSearchCriteria(
        String keyword,
        Double minPrice,
        Double maxPrice
) {
    public String normalizedKeyword() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim().toLowerCase();
    }
}
