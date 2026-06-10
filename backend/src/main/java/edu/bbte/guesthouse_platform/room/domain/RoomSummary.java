package edu.bbte.guesthouse_platform.room.domain;

import edu.bbte.guesthouse_platform.shared.domain.LocalizedText;

public record RoomSummary(
        String slug,
        String propertySlug,
        LocalizedText name,
        LocalizedText shortDescription,
        int capacity,
        boolean privateBathroom
) {
}
