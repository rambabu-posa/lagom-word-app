package com.lightbend.word.word.impl;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.*;
import com.lightbend.word.word.api.WordService;
import lombok.Value;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WordServiceImpl implements WordService {
    private final PersistentEntityRegistry persistentEntityRegistry;

    @Inject
    public WordServiceImpl(PersistentEntityRegistry persistentEntityRegistry, ActorSystem system) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        persistentEntityRegistry.register(WordEntity.class);

        Source<Pair<WordEvent.ProcessStarted, Offset>, NotUsed> source = persistentEntityRegistry.eventStream(WordEvent.WORD_EVENT_TAG, Offset.NONE)
                .filter(e -> e.first() instanceof WordEvent.ProcessStarted).map(p -> Pair.create((WordEvent.ProcessStarted) p.first(), p.second()));

        ActorMaterializer mat = ActorMaterializer.create(system);

        source.mapAsync(1, pair -> {

            PersistentEntityRef<WordCommand> ref = persistentEntityRegistry.refFor(WordEntity.class, pair.first().getUid());

            return translate(pair.first().getWord()).thenCompose(r -> {
                if (r instanceof TranslationResult.Success) {
                    return ref.ask(new WordCommand.AddTranslation("HashCode", ((TranslationResult.Success) r).getTranslation()));
                } else {
                    return ref.ask(new WordCommand.NoTranslation("HashCode", ((TranslationResult.Failure) r).getReason()));
                }
            });

        })
        .runWith(Sink.ignore(), mat);

    }

    private static CompletionStage<TranslationResult> translate(String word) {
        // it could be some service call
        if (Math.random() < 0.8) {
            return CompletableFuture.completedFuture(new TranslationResult.Success("T:" + word.hashCode()));
        } else {
            return CompletableFuture.completedFuture(new TranslationResult.Failure("No translation" ));
        }
    }

    @Override
    public ServiceCall<String, String> process() {
        return word -> {
            String id = UUID.randomUUID().toString();

            PersistentEntityRef<WordCommand> ref = persistentEntityRegistry.refFor(WordEntity.class, id);

            return ref.ask(new WordCommand.Process(id, word)).thenApply(done -> id);
        };
    }




}
