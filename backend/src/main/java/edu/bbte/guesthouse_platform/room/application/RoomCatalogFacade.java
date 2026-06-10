package edu.bbte.guesthouse_platform.room.application;

import edu.bbte.guesthouse_platform.room.domain.RoomSummary;
import edu.bbte.guesthouse_platform.shared.domain.LocalizedText;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomCatalogFacade {
    private static final List<RoomSummary> ROOMS = List.of(
            new RoomSummary(
                    "nisztor-family",
                    "nisztor",
                    LocalizedText.of("Családi szoba", "Cameră de familie", "Family room"),
                    LocalizedText.of(
                            "Kényelmes szoba családoknak és kisebb baráti társaságoknak.",
                            "Cameră confortabilă pentru familii și grupuri mici.",
                            "Comfortable room for families and small groups."
                    ),
                    4,
                    true
            ),
            new RoomSummary(
                    "bukovina-double",
                    "bukovina",
                    LocalizedText.of("Kétágyas szoba", "Cameră dublă", "Double room"),
                    LocalizedText.of(
                            "Egyszerű, nyugodt kétágyas szoba rövid tartózkodásokhoz.",
                            "Cameră dublă liniștită pentru șederi scurte.",
                            "Quiet double room for short stays."
                    ),
                    2,
                    true
            )
    );

    public List<RoomSummary> listRooms(String propertySlug) {
        if (propertySlug == null || propertySlug.isBlank()) {
            return ROOMS;
        }

        return ROOMS.stream()
                .filter(room -> room.propertySlug().equals(propertySlug))
                .toList();
    }
}
