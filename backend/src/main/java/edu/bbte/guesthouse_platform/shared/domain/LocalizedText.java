package edu.bbte.guesthouse_platform.shared.domain;

import java.util.EnumMap;
import java.util.Map;

public record LocalizedText(Map<LanguageCode, String> values) {
    public LocalizedText {
        values = Map.copyOf(values);
    }

    public static LocalizedText of(String hu, String ro, String en) {
        EnumMap<LanguageCode, String> values = new EnumMap<>(LanguageCode.class);
        values.put(LanguageCode.HU, hu);
        values.put(LanguageCode.RO, ro);
        values.put(LanguageCode.EN, en);
        return new LocalizedText(values);
    }

    public String resolve(LanguageCode languageCode) {
        return values.getOrDefault(languageCode, values.get(LanguageCode.HU));
    }
}
