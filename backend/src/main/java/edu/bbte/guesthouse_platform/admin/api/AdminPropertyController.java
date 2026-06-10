package edu.bbte.guesthouse_platform.admin.api;

import edu.bbte.guesthouse_platform.admin.application.PropertyManagementFacade;
import edu.bbte.guesthouse_platform.admin.domain.EditableContentArea;
import edu.bbte.guesthouse_platform.shared.api.ApiPaths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.ADMIN + "/properties")
public class AdminPropertyController {
    private final PropertyManagementFacade propertyManagementFacade;

    public AdminPropertyController(PropertyManagementFacade propertyManagementFacade) {
        this.propertyManagementFacade = propertyManagementFacade;
    }

    @GetMapping("/editable-content-areas")
    public List<EditableContentArea> listEditableContentAreas() {
        return propertyManagementFacade.listEditableContentAreas();
    }
}
