package com.lightbend.word.word.impl;

import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.word.word.api.WordService;

import java.util.UUID;

public class WordServiceImpl implements WordService {
    private final PersistentEntityRegistry persistentEntityRegistry;

    @Inject
    public WordServiceImpl(PersistentEntityRegistry persistentEntityRegistry) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        persistentEntityRegistry.register(WordEntity.class);
    }

    @Override
    public ServiceCall<String, String> process() {
        return word -> {
            String id = UUID.randomUUID().toString();

            PersistentEntityRef<WordCommand> ref = persistentEntityRegistry.refFor(WordEntity.class, id);

            return ref.ask(new WordCommand.Process(word)).thenApply(done -> id);
        };
    }

}
