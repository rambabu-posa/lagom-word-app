package com.lightbend.word.word.impl;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Translator {

    public static CompletionStage<TranslationResult> translate(String word) {
        String result = dictionary.get(word.toLowerCase());

        if (result != null) {
            return CompletableFuture.completedFuture(new TranslationResult.Success(result));
        } else {
            return CompletableFuture.completedFuture(new TranslationResult.Failure("No translation" ));
        }
    }

    private static PMap<String, String> dictionary = HashTreePMap.<String, String>empty()
            .plus("one", "uno")
            .plus("two", "dos")
            .plus("three", "tres");
}
