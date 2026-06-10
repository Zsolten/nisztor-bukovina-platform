package edu.bbte.guesthouse_platform.itinerary.api;

import edu.bbte.guesthouse_platform.itinerary.application.ItineraryRecommendationCommand;
import edu.bbte.guesthouse_platform.itinerary.application.ItineraryRecommendationFacade;
import edu.bbte.guesthouse_platform.itinerary.application.ItineraryRecommendationResult;
import edu.bbte.guesthouse_platform.shared.api.ApiPaths;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.PUBLIC + "/itinerary")
public class ItineraryRecommendationController {
    private final ItineraryRecommendationFacade itineraryRecommendationFacade;

    public ItineraryRecommendationController(ItineraryRecommendationFacade itineraryRecommendationFacade) {
        this.itineraryRecommendationFacade = itineraryRecommendationFacade;
    }

    @PostMapping("/recommendations")
    public ItineraryRecommendationResult recommend(@Valid @RequestBody ItineraryPreferenceRequest request) {
        return itineraryRecommendationFacade.recommend(new ItineraryRecommendationCommand(
                request.interests(),
                request.fitnessLevel(),
                request.availableHours(),
                request.departurePropertySlug(),
                request.transportMode(),
                request.pace(),
                request.language()
        ));
    }

    public record ItineraryPreferenceRequest(
            @NotEmpty List<String> interests,
            @NotBlank String fitnessLevel,
            @Min(1) @Max(12) int availableHours,
            @NotBlank String departurePropertySlug,
            @NotBlank String transportMode,
            @NotBlank String pace,
            @NotBlank String language
    ) {
    }
}
