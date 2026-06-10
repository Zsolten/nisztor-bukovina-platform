package edu.bbte.guesthouse_platform.itinerary.application;

import java.util.List;
import java.util.UUID;

public record ItineraryRecommendationResult(
        UUID recommendationId,
        String status,
        List<String> candidateRouteSlugs
) {
}
