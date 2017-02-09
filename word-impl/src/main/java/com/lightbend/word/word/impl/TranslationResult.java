package com.lightbend.word.word.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

public interface TranslationResult {

    @JsonDeserialize
    @Value
    class Success implements TranslationResult {
        String translation;
    }

    @JsonDeserialize
    @Value
    class Failure implements TranslationResult {
        String reason;
    }

}
