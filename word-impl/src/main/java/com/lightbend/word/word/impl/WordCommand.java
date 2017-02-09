package com.lightbend.word.word.impl;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;

import akka.Done;
import lombok.Value;

public interface WordCommand extends Jsonable {

    @SuppressWarnings("serial")
    @JsonDeserialize
    @Value
    class Process implements WordCommand, CompressedJsonable, PersistentEntity.ReplyType<Done> {
        String uid;
        String word;
    }

    @SuppressWarnings("serial")
    @JsonDeserialize
    @Value
    class AddTranslation implements WordCommand, CompressedJsonable, PersistentEntity.ReplyType<Done> {
        String language;
        String translation;
    }

    @JsonDeserialize
    @Value
    class NoTranslation implements WordCommand, CompressedJsonable, PersistentEntity.ReplyType<Done> {
        String language;
        String reason;
    }
}
