package com.lightbend.word.word.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import org.pcollections.HashTreePMap;

import java.util.Optional;

public class WordEntity extends PersistentEntity<WordCommand, WordEvent, WordState> {

    @Override
    public Behavior initialBehavior(Optional<WordState> snapshotState) {

        WordState initState = new WordState("", HashTreePMap.empty(), 0);

        BehaviorBuilder b = newBehaviorBuilder(initState);

        b.setCommandHandler(WordCommand.Process.class, (cmd, ctx) ->
                ctx.thenPersist(new WordEvent.ProcessStarted(entityId(), cmd.getWord()), evt ->
                        ctx.reply(Done.getInstance())
                )
        );

        b.setEventHandler(WordEvent.ProcessStarted.class, evt ->
                new WordState(evt.getWord(), HashTreePMap.empty(), 0)
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

        b.setCommandHandler(WordCommand.NoTranslation.class, (cmd, ctx) ->
            ctx.thenPersist(new WordEvent.TranslationFailure(entityId(), state().getWord(), cmd.getLanguage(), cmd.getReason()), evt -> {
                System.out.println("" + evt);
                ctx.reply(Done.getInstance());
            })
        );
        b.setEventHandler(WordEvent.TranslationFailure.class, evt ->
                state().builder().retries(state().getRetries() + 1).build()
        );

        return b.build();
    }
}
