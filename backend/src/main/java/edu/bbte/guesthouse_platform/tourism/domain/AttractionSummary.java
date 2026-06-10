package edu.bbte.guesthouse_platform.tourism.domain;

import edu.bbte.guesthouse_platform.shared.domain.LocalizedText;

public record AttractionSummary(
        String slug,
        LocalizedText name,
        LocalizedText practicalInfo,
        String category,
        int recommendedVisitMinutes,
        String difficulty
) {
}
