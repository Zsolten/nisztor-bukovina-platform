package edu.bbte.guesthouse_platform.tourism.api;

import edu.bbte.guesthouse_platform.shared.api.ApiPaths;
import edu.bbte.guesthouse_platform.tourism.application.TourismContentFacade;
import edu.bbte.guesthouse_platform.tourism.domain.AttractionSummary;
import edu.bbte.guesthouse_platform.tourism.domain.DayTripSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.PUBLIC + "/tourism")
public class TourismContentController {
    private final TourismContentFacade tourismContentFacade;

    public TourismContentController(TourismContentFacade tourismContentFacade) {
        this.tourismContentFacade = tourismContentFacade;
    }

    @GetMapping("/attractions")
    public List<AttractionSummary> listAttractions() {
        return tourismContentFacade.listAttractions();
    }

    @GetMapping("/day-trips")
    public List<DayTripSummary> listDayTrips() {
        return tourismContentFacade.listDayTrips();
    }
}
