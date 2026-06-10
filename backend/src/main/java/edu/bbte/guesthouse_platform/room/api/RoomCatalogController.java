package edu.bbte.guesthouse_platform.room.api;

import edu.bbte.guesthouse_platform.room.application.RoomCatalogFacade;
import edu.bbte.guesthouse_platform.room.domain.RoomSummary;
import edu.bbte.guesthouse_platform.shared.api.ApiPaths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.PUBLIC + "/rooms")
public class RoomCatalogController {
    private final RoomCatalogFacade roomCatalogFacade;

    public RoomCatalogController(RoomCatalogFacade roomCatalogFacade) {
        this.roomCatalogFacade = roomCatalogFacade;
    }

    @GetMapping
    public List<RoomSummary> listRooms(@RequestParam(name = "property", required = false) String propertySlug) {
        return roomCatalogFacade.listRooms(propertySlug);
    }
}
