package edu.bbte.guesthouse_platform.auth.application;

import edu.bbte.guesthouse_platform.auth.domain.AdminRole;

public record LoginResult(String accessToken, String tokenType, AdminRole role) {
}
