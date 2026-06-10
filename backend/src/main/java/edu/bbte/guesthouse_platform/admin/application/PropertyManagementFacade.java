package edu.bbte.guesthouse_platform.admin.application;

import edu.bbte.guesthouse_platform.admin.domain.EditableContentArea;
import edu.bbte.guesthouse_platform.admin.domain.GuesthouseSummary;
import edu.bbte.guesthouse_platform.shared.domain.LocalizedText;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyManagementFacade {
    private static final List<GuesthouseSummary> GUESTHOUSES = List.of(
            new GuesthouseSummary(
                    "nisztor",
                    "Nisztor panzio",
                    LocalizedText.of("Dél-Erdély", "Transilvania de Sud", "Southern Transylvania"),
                    LocalizedText.of(
                            "Családias panzió helyi programajánlókkal.",
                            "Pensiune primitoare cu recomandări locale.",
                            "Family-run guesthouse with local activity recommendations."
                    )
            ),
            new GuesthouseSummary(
                    "bukovina",
                    "Bukovina panzio",
                    LocalizedText.of("Dél-Erdély", "Transilvania de Sud", "Southern Transylvania"),
                    LocalizedText.of(
                            "Partnerpanzió közös foglalási és turisztikai tartalommal.",
                            "Pensiune parteneră cu rezervări și conținut turistic comun.",
                            "Partner guesthouse sharing booking and tourism content."
                    )
            )
    );

    public List<GuesthouseSummary> listPublicGuesthouses() {
        return GUESTHOUSES;
    }

    public Optional<GuesthouseSummary> findPublicGuesthouse(String slug) {
        return GUESTHOUSES.stream()
                .filter(guesthouse -> guesthouse.slug().equals(slug))
                .findFirst();
    }

    public List<EditableContentArea> listEditableContentAreas() {
        return List.of(
                new EditableContentArea("property-descriptions", "PROPERTY_OWNER", true),
                new EditableContentArea("room-descriptions", "PROPERTY_OWNER", true),
                new EditableContentArea("tourism-content", "PLATFORM_ADMIN", true),
                new EditableContentArea("day-trips", "PLATFORM_ADMIN", true)
        );
    }
}
