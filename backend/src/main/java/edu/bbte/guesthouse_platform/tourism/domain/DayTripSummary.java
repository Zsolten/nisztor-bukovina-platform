package edu.bbte.guesthouse_platform.tourism.domain;

import edu.bbte.guesthouse_platform.shared.domain.LocalizedText;

public record DayTripSummary(
        String slug,
        LocalizedText title,
        LocalizedText shortDescription,
        int estimatedHours,
        String startingPropertySlug
) {
}
