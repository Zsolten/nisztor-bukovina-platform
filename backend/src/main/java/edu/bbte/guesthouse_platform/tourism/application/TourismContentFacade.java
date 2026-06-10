package edu.bbte.guesthouse_platform.tourism.application;

import edu.bbte.guesthouse_platform.shared.domain.LocalizedText;
import edu.bbte.guesthouse_platform.tourism.domain.AttractionSummary;
import edu.bbte.guesthouse_platform.tourism.domain.DayTripSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TourismContentFacade {
    public List<AttractionSummary> listAttractions() {
        return List.of(
                new AttractionSummary(
                        "curated-attractions-from-pdf",
                        LocalizedText.of(
                                "PDF-ből felvett látnivalók",
                                "Obiective importate din PDF",
                                "Attractions imported from the PDF"
                        ),
                        LocalizedText.of(
                                "A konkrét pontok a helyi, ellenőrzött forrásból kerülnek fel.",
                                "Punctele concrete vor fi adăugate din sursa locală verificată.",
                                "Concrete points will be added from the verified local source."
                        ),
                        "CURATED_CONTENT",
                        90,
                        "EASY"
                )
        );
    }

    public List<DayTripSummary> listDayTrips() {
        return List.of(
                new DayTripSummary(
                        "daily-star-trip-template",
                        LocalizedText.of(
                                "Napi csillagtúra-váz",
                                "Schiță de excursie zilnică",
                                "Daily star-trip outline"
                        ),
                        LocalizedText.of(
                                "A pontos útvonalat a tárolt turisztikai pontok és utazási idők alapján állítja össze a rendszer.",
                                "Ruta exactă va fi compusă din punctele turistice și timpii de deplasare stocați.",
                                "The exact route will be composed from stored tourism points and travel times."
                        ),
                        6,
                        "nisztor"
                )
        );
    }
}
