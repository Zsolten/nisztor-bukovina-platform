package edu.bbte.guesthouse_platform.admin.domain;

import edu.bbte.guesthouse_platform.shared.domain.LocalizedText;

public record GuesthouseSummary(
        String slug,
        String name,
        LocalizedText location,
        LocalizedText shortDescription
) {
}
