package com.lightbend.word.word.impl;

import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.word.word.api.WordService;

import java.util.concurrent.CompletableFuture;

public class WordServiceImpl implements WordService {

    @Inject
    public WordServiceImpl() {

    }

    @Override
    public ServiceCall<String, String> process() {
        return request -> {
            return CompletableFuture.completedFuture(request + "!");
        };
    }

}
