package com.lightbend.word.word.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.*;
import com.lightbend.word.word.api.PostService;
import com.lightbend.word.word.api.WordService;
import org.pcollections.TreePVector;
import scala.concurrent.duration.FiniteDuration;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class WordServiceImpl implements WordService {
    private final PersistentEntityRegistry registry;

    @Inject
    public WordServiceImpl(PersistentEntityRegistry registry, PostService postService, ActorSystem system) {
        this.registry = registry;
        registry.register(WordEntity.class);

        Source<Pair<WordEvent.TranslationStarted, Offset>, NotUsed> source = registry.eventStream(WordEvent.WORD_EVENT_TAG, Offset.NONE)
                .filter(e -> e.first() instanceof WordEvent.TranslationStarted).map(p -> Pair.create((WordEvent.TranslationStarted) p.first(), p.second()));

        ActorMaterializer mat = ActorMaterializer.create(system);

        source
//            .throttle(1, FiniteDuration.apply(10, TimeUnit.SECONDS), 0, ThrottleMode.shaping())
            .mapAsync(1, pair -> {

            PersistentEntityRef<WordCommand> ref = registry.refFor(WordEntity.class, pair.first().getUid());

            return Translator.translate(pair.first().getWord()).thenCompose(r -> {
                if (r instanceof TranslationResult.Success) {
                    return ref.ask(new WordCommand.AddTranslation("Spanish", ((TranslationResult.Success) r).getTranslation()));
                } else {
                    return ref.ask(new WordCommand.NoTranslation("Spanish", ((TranslationResult.Failure) r).getReason()));
                }
            });

        })
        .runWith(Sink.ignore(), mat);

        postService.incomingWords()
                .subscribe()
                .atLeastOnce(
                    Flow.of(String.class)
                        .mapAsync(1, this::postWord)
                        .map(w -> Done.getInstance())
                );
    }

    private CompletionStage<String> postWord(String word) {
        String id = UUID.randomUUID().toString();

        PersistentEntityRef<WordCommand> ref = registry.refFor(WordEntity.class, id);

        return ref.ask(new WordCommand.Process(word)).thenApply(done -> id);
    }

    @Override
    public ServiceCall<String, String> process() {
        return this::postWord;
    }

    @Override
    public ServiceCall<NotUsed, String> getState(String id) {
        return nu -> {
            PersistentEntityRef<WordCommand> ref = registry.refFor(WordEntity.class, id);

            return ref.ask(new WordCommand.GetState()).thenApply(st ->
                    st.toString()
            );

        };
    }

    @Override
    public Topic<String> wordEvents() {
        return TopicProducer.taggedStreamWithOffset(TreePVector.singleton(WordEvent.WORD_EVENT_TAG),
                (tag, offset) -> registry.eventStream(tag, offset)
                        .map(eventAndOffset ->
                            Pair.create(eventAndOffset.first().toString(), eventAndOffset.second())
                ));
    }
}
