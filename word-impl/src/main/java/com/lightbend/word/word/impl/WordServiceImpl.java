package com.lightbend.word.word.impl;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.*;
import com.lightbend.word.word.api.WordService;
import org.pcollections.TreePVector;

import java.util.UUID;

public class WordServiceImpl implements WordService {
    private final PersistentEntityRegistry registry;

    @Inject
    public WordServiceImpl(PersistentEntityRegistry registry, ActorSystem system) {
        this.registry = registry;
        registry.register(WordEntity.class);

        Source<Pair<WordEvent.TranslationStarted, Offset>, NotUsed> source = registry.eventStream(WordEvent.WORD_EVENT_TAG, Offset.NONE)
                .filter(e -> e.first() instanceof WordEvent.TranslationStarted).map(p -> Pair.create((WordEvent.TranslationStarted) p.first(), p.second()));

        ActorMaterializer mat = ActorMaterializer.create(system);

        source.mapAsync(1, pair -> {

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

    }

    @Override
    public ServiceCall<String, String> process() {
        return word -> {
            String id = UUID.randomUUID().toString();

            PersistentEntityRef<WordCommand> ref = registry.refFor(WordEntity.class, id);

            return ref.ask(new WordCommand.Process(word)).thenApply(done -> id);
        };
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
                        .map(eventAndOffset -> {
                            System.out.println("eventAndOffset: " + eventAndOffset);
                            return Pair.create(eventAndOffset.first().toString(), eventAndOffset.second());
                        }
                ));
    }
}
