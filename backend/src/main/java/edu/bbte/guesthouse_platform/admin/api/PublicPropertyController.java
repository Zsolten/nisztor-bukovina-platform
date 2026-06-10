package edu.bbte.guesthouse_platform.admin.api;

import edu.bbte.guesthouse_platform.admin.application.PropertyManagementFacade;
import edu.bbte.guesthouse_platform.admin.domain.GuesthouseSummary;
import edu.bbte.guesthouse_platform.shared.api.ApiPaths;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.PUBLIC + "/properties")
public class PublicPropertyController {
    private final PropertyManagementFacade propertyManagementFacade;

    public PublicPropertyController(PropertyManagementFacade propertyManagementFacade) {
        this.propertyManagementFacade = propertyManagementFacade;
    }

    @GetMapping
    public List<GuesthouseSummary> listGuesthouses() {
        return propertyManagementFacade.listPublicGuesthouses();
    }

    @GetMapping("/{slug}")
    public GuesthouseSummary getGuesthouse(@PathVariable String slug) {
        return propertyManagementFacade.findPublicGuesthouse(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
