package edu.bbte.guesthouse_platform.auth.api;

import edu.bbte.guesthouse_platform.auth.application.AuthFacade;
import edu.bbte.guesthouse_platform.auth.application.LoginCommand;
import edu.bbte.guesthouse_platform.auth.application.LoginResult;
import edu.bbte.guesthouse_platform.shared.api.ApiPaths;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.ADMIN + "/auth")
public class AdminAuthController {
    private final AuthFacade authFacade;

    public AdminAuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @PostMapping("/login")
    public LoginResult login(@Valid @RequestBody LoginRequest request) {
        return authFacade.login(new LoginCommand(request.email(), request.password()));
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }
}
