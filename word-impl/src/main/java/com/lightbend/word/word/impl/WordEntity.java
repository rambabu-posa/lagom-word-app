package com.lightbend.word.word.impl;

import akka.Done;
import com.google.common.collect.Lists;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import org.pcollections.HashTreePMap;

import java.util.Optional;

public class WordEntity extends PersistentEntity<WordCommand, WordEvent, WordState> {

    @Override
    public Behavior initialBehavior(Optional<WordState> snapshotState) {

        WordState initState = new WordState("", HashTreePMap.empty(), 0);

        BehaviorBuilder b = newBehaviorBuilder(initState);

        b.setCommandHandler(WordCommand.Process.class, (cmd, ctx) ->
                ctx.thenPersist(new WordEvent.TranslationStarted(entityId(), cmd.getWord()), evt ->
                        ctx.reply(Done.getInstance())
                )
        );

        b.setEventHandler(WordEvent.TranslationStarted.class, evt ->
                new WordState(evt.getWord(), HashTreePMap.empty(), state().getRetries() + 1)
        );

        b.setCommandHandler(WordCommand.AddTranslation.class, (cmd, ctx) ->
            ctx.thenPersist(new WordEvent.Translated(cmd.getTranslation(), cmd.getLanguage()), evt -> {
                System.out.println("" + evt);
                ctx.reply(Done.getInstance());
            })
        );
        b.setEventHandler(WordEvent.Translated.class, evt ->
                state().addTranslation(evt.getLanguage(), evt.getTranslation())
        );

        b.setCommandHandler(WordCommand.NoTranslation.class, (cmd, ctx) -> {
            WordState st = state();
            if (state().getRetries() < 5) {
                WordEvent.TranslationStarted retry = new WordEvent.TranslationStarted(entityId(), state().getWord());
                return ctx.thenPersist(retry, evt -> {
                    System.out.println("" + evt + "\n > " + st + "\n > RETRY!");
                    ctx.reply(Done.getInstance());
                });
            } else {
                WordEvent.TranslationFailure translationFailure = new WordEvent.TranslationFailure(entityId(), state().getWord(), cmd.getLanguage(), cmd.getReason());
                return ctx.thenPersist(translationFailure, evt -> {
                    System.out.println("" + evt + "\n > " + st + "\n > NO MORE RETRIES!");
                    ctx.reply(Done.getInstance());
                });
            }
        });
        b.setEventHandler(WordEvent.TranslationFailure.class, evt ->
                state()
        );

        return b.build();
    }
}
