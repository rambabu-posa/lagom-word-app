package com.lightbend.word.word.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import org.pcollections.HashTreePMap;

import java.util.Optional;

public class WordEntity extends PersistentEntity<WordCommand, WordEvent, WordState> {

    @Override
    public Behavior initialBehavior(Optional<WordState> snapshotState) {

        WordState initState = new WordState("", HashTreePMap.empty());

        BehaviorBuilder b = newBehaviorBuilder(initState);

        b.setCommandHandler(WordCommand.Process.class, (cmd, ctx) ->
                ctx.thenPersist(new WordEvent.Init(cmd.getUid(), cmd.getWord()), evt ->
                        ctx.reply(Done.getInstance())
                )
        );

        b.setEventHandler(WordEvent.Init.class, evt ->
                new WordState(evt.getWord(), HashTreePMap.empty())
        );

        b.setEventHandler(WordEvent.Translated.class, evt ->
                state().addTranslation(evt.getLanguage(), evt.getTranslation())
        );

        b.setCommandHandler(WordCommand.AddTranslation.class, (cmd, ctx) ->
            ctx.thenPersist(new WordEvent.Translated(cmd.getTranslation(), cmd.getLanguage()), evt -> {
                System.out.println("" + evt);
                ctx.reply(Done.getInstance());
            })
        );

        return b.build();
    }
}
