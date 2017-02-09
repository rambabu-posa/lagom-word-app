package com.lightbend.word.word.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;
import org.pcollections.PMap;

@SuppressWarnings("serial")
@JsonDeserialize
@Value
@Builder
final public class WordState {
    String word;
    PMap<String, String> translations;
    int retries;

    public WordState addTranslation(String language, String translation) {
        return builder().translations(getTranslations().plus(language, translation)).build();
    }
}
