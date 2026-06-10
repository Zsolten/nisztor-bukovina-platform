package edu.bbte.guesthouse_platform.auth.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthFacade {
    public LoginResult login(LoginCommand command) {
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "JWT based admin authentication is reserved for the next implementation step."
        );
    }
}
