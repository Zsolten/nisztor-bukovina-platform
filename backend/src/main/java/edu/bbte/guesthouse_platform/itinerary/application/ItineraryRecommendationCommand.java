package edu.bbte.guesthouse_platform.itinerary.application;

import java.util.List;

public record ItineraryRecommendationCommand(
        List<String> interests,
        String fitnessLevel,
        int availableHours,
        String departurePropertySlug,
        String transportMode,
        String pace,
        String language
) {
}
