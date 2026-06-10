package edu.bbte.guesthouse_platform.itinerary.application;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ItineraryRecommendationFacade {
    public ItineraryRecommendationResult recommend(ItineraryRecommendationCommand command) {
        return new ItineraryRecommendationResult(
                UUID.randomUUID(),
                "PREFILTER_READY",
                List.of("daily-star-trip-template")
        );
    }
}
