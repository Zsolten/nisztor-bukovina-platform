package edu.bbte.guesthouse_platform.shared.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

public enum LanguageCode {
    HU("hu"),
    RO("ro"),
    EN("en");

    private final String tag;

    LanguageCode(String tag) {
        this.tag = tag;
    }

    @JsonValue
    public String tag() {
        return tag;
    }

    public static Optional<LanguageCode> fromTag(String tag) {
        return Arrays.stream(values())
                .filter(languageCode -> languageCode.tag.equalsIgnoreCase(tag))
                .findFirst();
    }
}
