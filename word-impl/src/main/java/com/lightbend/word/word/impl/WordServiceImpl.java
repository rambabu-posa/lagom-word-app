package com.lightbend.word.word.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.japi.pf.PFBuilder;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.*;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.word.word.api.WordService;
import org.pcollections.PCollection;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class WordServiceImpl implements WordService {
    private final PersistentEntityRegistry persistentEntityRegistry;

    @Inject
    public WordServiceImpl(PersistentEntityRegistry persistentEntityRegistry, ActorSystem system) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        persistentEntityRegistry.register(WordEntity.class);

        Source<Pair<WordEvent.Init, Offset>, NotUsed> source = persistentEntityRegistry.eventStream(WordEvent.WORD_EVENT_TAG, Offset.NONE)
                .filter(e -> e.first() instanceof WordEvent.Init).map(p -> Pair.create((WordEvent.Init) p.first(), p.second()));

        ActorMaterializer mat = ActorMaterializer.create(system);

        source.mapAsync(1, pair -> {

            PersistentEntityRef<WordCommand> ref = persistentEntityRegistry.refFor(WordEntity.class, pair.first().getUid());

            return ref.ask(new WordCommand.AddTranslation("HashCode language", "T:" + pair.first().getWord().hashCode()));

        })
        .runWith(Sink.ignore(), mat);

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
